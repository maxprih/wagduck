package org.maxpri.wagduck.generator.go.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface GoEntityMapper {

    @Mapping(target = "packageName", expression = "java(deriveGoPackageName(config, entity))")
    @Mapping(target = "structName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "tableName", source = "entity", qualifiedByName = "determineGoTableName")
    @Mapping(target = "embedGormModel", expression = "java(checkGoProjectOption(config, \"EMBED_GORM_MODEL\", true))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectGoImports")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapGoAttributes")
    @Mapping(target = "relationships", expression = "java(mapGoRelationships(config, entity))")
    @Mapping(target = "idField", source = "entity", qualifiedByName = "generateGoIdField")
    @Mapping(target = "createdAtField", expression = "java(generateGoTimestampField(config, \"CreatedAt\", \"autoCreateTime\"))")
    @Mapping(target = "updatedAtField", expression = "java(generateGoTimestampField(config, \"UpdatedAt\", \"autoUpdateTime\"))")
    GoEntityModel toGoEntityModel(ProjectConfiguration config, EntityDefinition entity);

    default String deriveGoPackageName(ProjectConfiguration config, EntityDefinition entity) {
        return "model";
    }

    @Named("determineGoTableName")
    default String determineGoTableName(EntityDefinition entity) {
        if (entity.getTableName() != null && !entity.getTableName().isBlank()) {
            return entity.getTableName();
        }
        return NamingUtils.toSnakeCase(NamingUtils.toSnakeCase(entity.getEntityName()));
    }

    @Named("mapGoAttributes")
    default List<GoFieldModel> mapGoAttributes(List<AttributeDefinition> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }

        ProjectConfiguration config = attributes.get(0).getEntityDefinition().getProjectConfiguration();

        boolean embedGormModel = checkGoProjectOption(config, "EMBED_GORM_MODEL", true);
        boolean generateIdFieldSeparately = !embedGormModel;
        boolean generateCreatedAtFieldSeparately = !embedGormModel && checkGoProjectOption(config, "ADD_CREATEDAT_FIELD", true);
        boolean generateUpdatedAtFieldSeparately = !embedGormModel && checkGoProjectOption(config, "ADD_UPDATEDAT_FIELD", true);

        return attributes.stream()
                .filter(attr -> {
                    if (generateIdFieldSeparately && attr.isPrimaryKey()) {
                        return false;
                    }

                    String attrNameLower = attr.getAttributeName().toLowerCase();
                    String colNameLower = attr.getColumnName() != null ? attr.getColumnName().toLowerCase() : "";
                    if (generateCreatedAtFieldSeparately &&
                            (attrNameLower.equals("createdat") || attrNameLower.equals("created_at") || colNameLower.equals("created_at"))) {
                        return false;
                    }
                    if (generateUpdatedAtFieldSeparately &&
                            (attrNameLower.equals("updatedat") || attrNameLower.equals("updated_at") || colNameLower.equals("updated_at"))) {
                        return false;
                    }
                    if (embedGormModel) {
                        if (attr.isPrimaryKey() ||
                                colNameLower.equals("created_at") ||
                                colNameLower.equals("updated_at") ||
                                colNameLower.equals("deleted_at")) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(attr -> mapSingleGoAttribute(attr, attr.getEntityDefinition().getProjectConfiguration()))
                .collect(Collectors.toList());
    }

    default GoFieldModel mapSingleGoAttribute(AttributeDefinition attr, ProjectConfiguration config) {
        String goType = mapToGoDataType(attr.getDataType());
        String fieldName = NamingUtils.toPascalCase(attr.getAttributeName());
        String columnName = attr.getColumnName() != null && !attr.getColumnName().isBlank() ? attr.getColumnName() : NamingUtils.toSnakeCase(attr.getAttributeName());

        List<String> gormParts = new ArrayList<>();
        if (attr.isPrimaryKey() && !checkGoProjectOption(config, "EMBED_GORM_MODEL", true)) {
            gormParts.add("primaryKey");
            if (isIntegerType(goType)) gormParts.add("autoIncrement");
            else if ("uuid.UUID".equals(goType)) gormParts.add("type:uuid;default:gen_random_uuid()");
        }

        if (!columnName.equals(NamingUtils.toSnakeCase(fieldName.replaceAll("ID$", ""))) && !attr.isPrimaryKey()) {
            gormParts.add("column:" + columnName);
        }

        if (attr.isRequired() && !attr.isPrimaryKey()) {
            gormParts.add("not null");
        }
        if (attr.isUnique()) {
            gormParts.add("unique");
        }

        String gormTag = String.join(";", gormParts);
        String jsonTag = NamingUtils.toCamelCase(attr.getAttributeName()) + ",omitempty";

        return GoFieldModel.builder()
                .name(fieldName)
                .type(goType)
                .gormTag(gormTag.isEmpty() ? "" : "gorm:\"" + gormTag + "\"")
                .jsonTag("json:\"" + jsonTag + "\"")
                .isRelationship(false)
                .build();
    }

    default boolean isIntegerType(String goType) {
        return Arrays.asList("int", "int8", "int16", "int32", "int64", "uint", "uint8", "uint16", "uint32", "uint64").contains(goType);
    }


    default List<GoFieldModel> mapGoRelationships(ProjectConfiguration config, EntityDefinition currentEntity) {
        if (currentEntity.getRelationships() == null) return Collections.emptyList();
        List<GoFieldModel> goFields = new ArrayList<>();

        for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
            EntityDefinition targetEntityDef = findEntity(config, relDef.getTargetEntity().getEntityName());
            if (targetEntityDef == null) {
                System.err.println("Warning: Target entity " + relDef.getTargetEntity().getEntityName() + " not found for relationship in " + currentEntity.getEntityName());
                continue;
            }

            String sourceFieldName = NamingUtils.toPascalCase(relDef.getSourceFieldName());
            String targetStructName = NamingUtils.toPascalCase(targetEntityDef.getEntityName());
            GoFieldModel.GoFieldModelBuilder associationFieldBuilder = GoFieldModel.builder()
                    .name(sourceFieldName)
                    .jsonTag("json:\"" + NamingUtils.toCamelCase(relDef.getSourceFieldName()) + ",omitempty\"")
                    .isRelationship(true);

            List<String> gormPartsAssociation = new ArrayList<>();
            GoFieldModel.GoFieldModelBuilder fkFieldBuilder = null;

            String goFkFieldName = "";

            switch (relDef.getRelationshipType()) {
                case ONE_TO_ONE:
                    associationFieldBuilder.type("*" + targetStructName).isPointer(true);
                    if (relDef.isOwningSide()) {
                        goFkFieldName = sourceFieldName + "ID";

                        String dbFkColumnName = (relDef.getJoinColumnName() != null && !relDef.getJoinColumnName().isBlank())
                                ? relDef.getJoinColumnName()
                                : NamingUtils.toSnakeCase(goFkFieldName);

                        List<String> gormPartsFkField = new ArrayList<>();
                        gormPartsFkField.add("column:" + dbFkColumnName);

                        fkFieldBuilder = GoFieldModel.builder()
                                .name(goFkFieldName)
                                .type("*" + getPrimaryKeyGoType(targetEntityDef, config))
                                .jsonTag("json:\"" + NamingUtils.toCamelCase(goFkFieldName) + ",omitempty\"")
                                .gormTag("gorm:\"" + String.join(";", gormPartsFkField) + "\"")
                                .isRelationship(false);

                        gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                        String targetPkFieldName = findPrimaryKeyFieldName(targetEntityDef);
                        if (!"ID".equals(targetPkFieldName)) {
                            gormPartsAssociation.add("references:" + targetPkFieldName);
                        }

                    } else {
                        goFkFieldName = NamingUtils.toPascalCase(currentEntity.getEntityName()) + "ID";
                        gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                        String currentPkFieldName = findPrimaryKeyFieldName(currentEntity);
                        if (!"ID".equals(currentPkFieldName)) {
                            gormPartsAssociation.add("references:" + currentPkFieldName);
                        }
                    }
                    break;

                case MANY_TO_ONE:
                    associationFieldBuilder.type("*" + targetStructName).isPointer(true);
                    goFkFieldName = targetStructName + "ID";
                    String dbFkColumnNameMTO = (relDef.getJoinColumnName() != null && !relDef.getJoinColumnName().isBlank())
                            ? relDef.getJoinColumnName()
                            : NamingUtils.toSnakeCase(goFkFieldName);

                    List<String> gormPartsFkFieldMTO = new ArrayList<>();
                    gormPartsFkFieldMTO.add("column:" + dbFkColumnNameMTO);

                    fkFieldBuilder = GoFieldModel.builder()
                            .name(goFkFieldName)
                            .type("*" + getPrimaryKeyGoType(targetEntityDef, config))
                            .jsonTag("json:\"" + NamingUtils.toCamelCase(goFkFieldName) + ",omitempty\"")
                            .gormTag("gorm:\"" + String.join(";", gormPartsFkFieldMTO) + "\"")
                            .isRelationship(false);

                    gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                    String targetPkFieldNameMTO = findPrimaryKeyFieldName(targetEntityDef);
                    if (!"ID".equals(targetPkFieldNameMTO)) {
                        gormPartsAssociation.add("references:" + targetPkFieldNameMTO);
                    }
                    break;

                case ONE_TO_MANY:
                    associationFieldBuilder.type("[]" + targetStructName).isSlice(true);
                    goFkFieldName = NamingUtils.toPascalCase(currentEntity.getEntityName()) + "ID";
                    gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                    String currentPkFieldNameOTM = findPrimaryKeyFieldName(currentEntity);
                    if (!"ID".equals(currentPkFieldNameOTM)) {
                        gormPartsAssociation.add("references:" + currentPkFieldNameOTM);
                    }
                    break;

                case MANY_TO_MANY:
                    associationFieldBuilder.type("[]" + targetStructName).isSlice(true);
                    String joinTableName = relDef.getJoinTableName();
                    if (joinTableName == null || joinTableName.isBlank()) {
                        List<String> names = Arrays.asList(NamingUtils.toSnakeCase(currentEntity.getEntityName()), NamingUtils.toSnakeCase(targetEntityDef.getEntityName()));
                        Collections.sort(names);
                        joinTableName = names.get(0) + "_" + names.get(1);
                    }
                    gormPartsAssociation.add("many2many:" + joinTableName);
                    break;
                default:
                    associationFieldBuilder.type("interface{}");
            }
            if (fkFieldBuilder != null) {
                goFields.add(fkFieldBuilder.build());
            }
            String gormTagAssociation = String.join(";", gormPartsAssociation);
            associationFieldBuilder.gormTag(gormTagAssociation.isEmpty() ? "" : "gorm:\"" + gormTagAssociation + "\"");
            goFields.add(associationFieldBuilder.build());
        }
        return goFields;
    }

    default String findPrimaryKeyFieldName(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .map(attr -> NamingUtils.toPascalCase(attr.getAttributeName()))
                .findFirst().orElse("ID");
    }

    default String getPrimaryKeyGoType(EntityDefinition entity, ProjectConfiguration config) {
        if (checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true)) {
            return "uint";
        }
        AttributeDefinition pkAttribute = entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst().orElse(null);

        if (pkAttribute != null) {
            return mapToGoDataType(pkAttribute.getDataType());
        }
        return "uint";
    }

    @Named("collectGoImports")
    default Set<String> collectGoImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        boolean embedGorm = checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true);

        boolean hasTimeField = false;
        if (!embedGorm) {
            if (checkGoProjectOption(entity.getProjectConfiguration(), "ADD_CREATED_AT_FIELD", true) || checkGoProjectOption(entity.getProjectConfiguration(), "ADD_UPDATED_AT_FIELD", true)) {
                hasTimeField = true;
            }
            if (entity.getAttributes().stream().anyMatch(attr -> attr.isPrimaryKey() && "uuid".equalsIgnoreCase(attr.getDataType()))) {
                imports.add("github.com/google/uuid");
            }
        }

        for (AttributeDefinition attr : entity.getAttributes()) {
            String goType = mapToGoDataType(attr.getDataType());
            if (goType.startsWith("time.Time") || goType.startsWith("*time.Time")) hasTimeField = true;
            if (goType.equals("uuid.UUID")) imports.add("github.com/google/uuid");
            if (goType.equals("decimal.Decimal")) imports.add("github.com/shopspring/decimal");
        }

        if (hasTimeField) {
            imports.add("time");
        }

        return imports;
    }


    @Named("generateGoIdField")
    default GoFieldModel generateGoIdField(EntityDefinition entity) {
        if (checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true)) {
            return null;
        }
        AttributeDefinition pkAttribute = entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst().orElse(null);

        if (pkAttribute == null) {
            return GoFieldModel.builder()
                    .name("ID")
                    .type("uint")
                    .gormTag("gorm:\"primaryKey;autoIncrement\"")
                    .jsonTag("json:\"id\"")
                    .description("Unique identifier for the record.")
                    .build();
        }

        String goType = mapToGoDataType(pkAttribute.getDataType());
        String fieldName = NamingUtils.toPascalCase(pkAttribute.getAttributeName());
        String gormTag = "gorm:\"primaryKey";
        if (isIntegerType(goType)) gormTag += ";autoIncrement";
        else if ("uuid.UUID".equals(goType)) gormTag += ";type:uuid;default:gen_random_uuid()";
        gormTag += "\"";

        return GoFieldModel.builder()
                .name(fieldName)
                .type(goType)
                .gormTag(gormTag)
                .jsonTag("json:\"" + NamingUtils.toCamelCase(pkAttribute.getAttributeName()) + "\"")
                .build();
    }

    default GoFieldModel generateGoTimestampField(ProjectConfiguration config, String fieldName, String gormTimeFunction) {
        if (checkGoProjectOption(config, "EMBED_GORM_MODEL", true)) {
            return null;
        }
        if (!checkGoProjectOption(config, "ADD_" + fieldName.toUpperCase() + "_FIELD", true)) {
            return null;
        }

        return GoFieldModel.builder()
                .name(fieldName)
                .type("time.Time")
                .gormTag("gorm:\"" + gormTimeFunction + "\"")
                .jsonTag("json:\"" + NamingUtils.toCamelCase(fieldName) + "\"")
                .description(fieldName.equals("CreatedAt") ? "Timestamp of record creation." : "Timestamp of last record update.")
                .build();
    }

    default String mapToGoDataType(String javaDataType) {
        if (javaDataType == null) return "interface{}";
        return switch (javaDataType.toLowerCase()) {
            case "string", "text", "varchar" -> "string";
            case "integer", "int" -> "int";
            case "long", "bigint" -> "int64";
            case "double" -> "float64";
            case "float" -> "float32";
            case "decimal", "numeric" -> "decimal.Decimal";
            case "boolean", "bool" -> "bool";
            case "date", "timestamp", "datetime" -> "time.Time";
            case "time" -> "time.Time";
            case "uuid" -> "uuid.UUID";
            case "blob", "bytea" -> "[]byte";
            default -> "string";
        };
    }

    default boolean checkGoProjectOption(ProjectConfiguration config, String optionKey, boolean defaultValue) {
        if ("EMBED_GORM_MODEL".equals(optionKey)) return false;
        if (optionKey.startsWith("ADD_") && optionKey.endsWith("_FIELD")) return true;
        return defaultValue;
    }

    default EntityDefinition findEntity(ProjectConfiguration config, String entityName) {
        return config.getEntities().stream()
                .filter(e -> e.getEntityName().equalsIgnoreCase(entityName))
                .findFirst().orElse(null);
    }
}