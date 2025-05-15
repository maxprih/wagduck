package org.maxpri.wagduck.generator.kotlin.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.domain.enums.ProjectOptions;
import org.maxpri.wagduck.domain.enums.RelationshipType;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface KotlinEntityMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName())")
    @Mapping(target = "tableName", source = "entity", qualifiedByName = "determineTableNameKt")
    @Mapping(target = "includeAuditing", expression = "java(org.maxpri.wagduck.util.NamingUtils.checkProjectOption(config, org.maxpri.wagduck.domain.enums.ProjectOptions.ENABLE_JPA_AUDITING))")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributesKt")
    @Mapping(target = "relationships", expression = "java(mapAllRelationshipsKt(config, entity))")
    @Mapping(target = "classAnnotations", source = "entity", qualifiedByName = "generateEntityClassAnnotationsKt")
    @Mapping(target = "imports", expression = "java(collectEntityImportsKt(config, entity))")
    @Mapping(target = "createdAtAttribute", source = "entity", qualifiedByName = "generateCreatedAtAttributeKt")
    @Mapping(target = "updatedAtAttribute", source = "entity", qualifiedByName = "generateUpdatedAtAttributeKt")
    KotlinEntityModel toKotlinEntityModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("determineTableNameKt")
    default String determineTableNameKt(EntityDefinition entity) {
        if (entity.getTableName() != null && !entity.getTableName().isBlank()) {
            return entity.getTableName();
        }
        return NamingUtils.toSnakeCase(entity.getEntityName());
    }

    @Named("mapAttributesKt")
    default List<KotlinAttributeModel> mapAttributesKt(List<AttributeDefinition> attributes) {
        if (attributes == null) return Collections.emptyList();
        return attributes.stream()
                .map(this::mapSingleAttributeKt)
                .collect(Collectors.toList());
    }

    default KotlinAttributeModel mapSingleAttributeKt(AttributeDefinition attr) {
        List<String> annotations = new ArrayList<>();
        String baseKotlinType = mapBaseKotlinType(attr.getDataType());
        String attributeName = NamingUtils.toCamelCase(attr.getAttributeName());
        boolean isNullable = !attr.isRequired();
        String initializer = "null";

        if (attr.isPrimaryKey()) {
            annotations.add("@Id");
            isNullable = true;
            String strategy = "GenerationType.IDENTITY";
            if ("java.util.UUID".equals(baseKotlinType)) {
                strategy = "GenerationType.UUID";
            }
            annotations.add(String.format("@GeneratedValue(strategy = %s)", strategy));
        } else {
            if (!isNullable) {
                isNullable = true;
                initializer = "null";
            }
        }


        List<String> columnParams = new ArrayList<>();
        String dbColumnName = attr.getColumnName() != null ? attr.getColumnName() : NamingUtils.toSnakeCase(attributeName);
        if (!dbColumnName.equals(NamingUtils.toSnakeCase(attributeName))) {
            columnParams.add(String.format("name = \"%s\"", dbColumnName));
        }

        if (attr.isRequired() && !attr.isPrimaryKey()) {
            columnParams.add("nullable = false");
        }
        if (attr.isUnique()) {
            columnParams.add("unique = true");
        }
        if (attr.isRequired()) {
            if ("String".equals(baseKotlinType)) {
                annotations.add("@field:NotBlank");
            } else {
                annotations.add("@field:NotNull");
            }
        }


        if (!columnParams.isEmpty()) {
            annotations.add(String.format("@Column(%s)", String.join(", ", columnParams)));
        }
        if (attr.isPrimaryKey()) {
            initializer = "null";
        }


        return KotlinAttributeModel.builder()
                .name(attributeName)
                .baseKotlinType(baseKotlinType)
                .isNullable(isNullable)
                .isPrimaryKey(attr.isPrimaryKey())
                .annotations(annotations)
                .initializer(initializer)
                .build();
    }

    default List<KotlinRelationshipModel> mapAllRelationshipsKt(ProjectConfiguration config, EntityDefinition currentEntity) {
        List<KotlinRelationshipModel> result = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        if (currentEntity.getRelationships() != null) {
            for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
                KotlinRelationshipModel relModel = mapSingleRelationshipKt(relDef, currentEntity, relDef.getTargetEntity());
                if (usedNames.add(relModel.getName())) {
                    result.add(relModel);
                }
            }
        }
        List<EntityDefinition> allEntities = config.getEntities();
        for (EntityDefinition otherEntity : allEntities) {
            if (otherEntity.equals(currentEntity) || otherEntity.getRelationships() == null) {
                continue;
            }
            for (RelationshipDefinition relDefOnOther : otherEntity.getRelationships()) {
                if (relDefOnOther.getTargetEntity() != null &&
                        relDefOnOther.getTargetEntity().getEntityName().equals(currentEntity.getEntityName()) &&
                        relDefOnOther.getTargetFieldName() != null && !relDefOnOther.getTargetFieldName().isBlank()) {

                    KotlinRelationshipModel inverseModel = mapInverseRelationshipKt(relDefOnOther, otherEntity, currentEntity);
                    if (usedNames.add(inverseModel.getName())) {
                        result.add(inverseModel);
                    }
                }
            }
        }
        return result;
    }


    default KotlinRelationshipModel mapSingleRelationshipKt(RelationshipDefinition rel, EntityDefinition sourceEntity, EntityDefinition targetEntity) {
        String relationshipName = NamingUtils.toCamelCase(rel.getSourceFieldName());
        String targetClassName = NamingUtils.toPascalCase(targetEntity.getEntityName());
        String annotation;
        String fieldBaseType;
        String initializer = "null";
        boolean isFieldNullable = true;

        String fetchTypeStr = "FetchType." + rel.getFetchType().name();

        switch (rel.getRelationshipType()) {
            case ONE_TO_ONE:
                fieldBaseType = targetClassName;
                if (rel.isOwningSide()) {
                    String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                    annotation = String.format("@OneToOne(fetch = %s)\n    @JoinColumn(name = \"%s\", unique = true)",
                            fetchTypeStr, joinColumn);
                } else {
                    String mappedBy = NamingUtils.toCamelCase(rel.getTargetFieldName());
                    annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = %s)", mappedBy, fetchTypeStr);
                }
                break;
            case MANY_TO_ONE:
                fieldBaseType = targetClassName;
                String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                annotation = String.format("@ManyToOne(fetch = %s)\n    @JoinColumn(name = \"%s\")",
                        fetchTypeStr, joinColumn);
                break;
            case ONE_TO_MANY:
                fieldBaseType = String.format("MutableSet<%s>", targetClassName);
                String mappedByOneToMany = NamingUtils.toCamelCase(rel.getTargetFieldName());
                annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = %s, orphanRemoval = true, cascade = [CascadeType.ALL])",
                        mappedByOneToMany, fetchTypeStr);
                initializer = "mutableSetOf()";
                isFieldNullable = false;
                break;
            case MANY_TO_MANY:
                fieldBaseType = String.format("MutableSet<%s>", targetClassName);
                if (rel.isOwningSide()) {
                    String joinTableName = rel.getJoinTableName() != null ? rel.getJoinTableName() : NamingUtils.toSnakeCase(sourceEntity.getEntityName()) + "_" + NamingUtils.toSnakeCase(targetClassName);
                    String sourceJoinCol = rel.getJoinTableSourceColumnName() != null ? rel.getJoinTableSourceColumnName() : NamingUtils.toSnakeCase(sourceEntity.getEntityName()) + "_id";
                    String targetJoinCol = rel.getJoinTableTargetColumnName() != null ? rel.getJoinTableTargetColumnName() : NamingUtils.toSnakeCase(targetEntity.getEntityName()) + "_id";
                    annotation = String.format("@ManyToMany(fetch = %s, cascade = [CascadeType.PERSIST, CascadeType.MERGE])\n    @JoinTable(\n        name = \"%s\",\n        joinColumns = [JoinColumn(name = \"%s\")],\n        inverseJoinColumns = [JoinColumn(name = \"%s\")]\n    )",
                            fetchTypeStr, joinTableName, sourceJoinCol, targetJoinCol);
                } else {
                    String mappedByManyToMany = NamingUtils.toCamelCase(rel.getTargetFieldName());
                    annotation = String.format("@ManyToMany(mappedBy = \"%s\", fetch = %s)", mappedByManyToMany, fetchTypeStr);
                }
                initializer = "mutableSetOf()";
                isFieldNullable = false;
                break;
            default:
                throw new IllegalArgumentException("Unsupported relationship type: " + rel.getRelationshipType());
        }

        return KotlinRelationshipModel.builder()
                .name(relationshipName)
                .baseKotlinType(fieldBaseType)
                .isNullable(isFieldNullable)
                .annotations(List.of(annotation))
                .initializer(initializer)
                .build();
    }

    default KotlinRelationshipModel mapInverseRelationshipKt(RelationshipDefinition originalRel, EntityDefinition originalSourceEntity, EntityDefinition currentEntityAsTarget) {
        String inverseFieldName = NamingUtils.toCamelCase(originalRel.getTargetFieldName());
        String originalSourceClassName = NamingUtils.toPascalCase(originalSourceEntity.getEntityName());
        String annotation;
        String fieldBaseType;
        String initializer = "null";
        boolean isFieldNullable = true;

        String fetchTypeStr = "FetchType." + originalRel.getFetchType().name();

        switch (originalRel.getRelationshipType()) {
            case ONE_TO_MANY:
                fieldBaseType = originalSourceClassName;
                String joinColumnNameForManyToOne = originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(originalRel.getSourceFieldName()) + "_id";
                String mappedByFieldOnOriginalSource = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                annotation = String.format("@ManyToOne(fetch = %s)\n    @JoinColumn(name = \"%s\")",
                        fetchTypeStr,
                        originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(inverseFieldName) + "_id"
                );
                break;

            case MANY_TO_ONE:
                fieldBaseType = String.format("MutableSet<%s>", originalSourceClassName);
                String mappedByFieldManyToOne = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = %s, orphanRemoval = true, cascade = [CascadeType.ALL])",
                        mappedByFieldManyToOne, fetchTypeStr);
                initializer = "mutableSetOf()";
                isFieldNullable = false;
                break;

            case ONE_TO_ONE:
                fieldBaseType = originalSourceClassName;
                String mappedByOneToOne = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                if (originalRel.isOwningSide()) {
                    annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = %s)", mappedByOneToOne, fetchTypeStr);
                } else {
                    String joinColumnNameO2O = originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(mappedByOneToOne) + "_id";
                    annotation = String.format("@OneToOne(fetch = %s)\n    @JoinColumn(name = \"%s\", unique = true)",
                            fetchTypeStr, joinColumnNameO2O);
                }
                break;

            case MANY_TO_MANY:
                fieldBaseType = String.format("MutableSet<%s>", originalSourceClassName);
                String mappedByManyToMany = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                annotation = String.format("@ManyToMany(mappedBy = \"%s\", fetch = %s)",
                        mappedByManyToMany, fetchTypeStr);
                initializer = "mutableSetOf()";
                isFieldNullable = false;
                break;
            default:
                throw new IllegalArgumentException("Unsupported relationship type for inverse mapping: " + originalRel.getRelationshipType());
        }

        return KotlinRelationshipModel.builder()
                .name(inverseFieldName)
                .baseKotlinType(fieldBaseType)
                .isNullable(isFieldNullable)
                .annotations(List.of(annotation))
                .initializer(initializer)
                .build();
    }


    @Named("generateEntityClassAnnotationsKt")
    default List<String> generateEntityClassAnnotationsKt(EntityDefinition entity) {
        List<String> annotations = new ArrayList<>();
        annotations.add("@Entity");
        annotations.add(String.format("@Table(name = \"%s\")", determineTableNameKt(entity)));

        boolean includeAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(), ProjectOptions.ENABLE_JPA_AUDITING);
        if (includeAuditing) {
            annotations.add("@EntityListeners(AuditingEntityListener::class)");
        }
        return annotations;
    }

    @Named("generateCreatedAtAttributeKt")
    default KotlinAttributeModel generateCreatedAtAttributeKt(EntityDefinition entity) {
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(), ProjectOptions.ENABLE_JPA_AUDITING);
        if (!enableAuditing) return null;

        return KotlinAttributeModel.builder()
                .name("createdAt")
                .baseKotlinType("java.time.LocalDateTime")
                .isNullable(true)
                .annotations(List.of("@CreatedDate", "@Column(nullable = false, updatable = false)"))
                .initializer("null")
                .build();
    }

    @Named("generateUpdatedAtAttributeKt")
    default KotlinAttributeModel generateUpdatedAtAttributeKt(EntityDefinition entity) {
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(), ProjectOptions.ENABLE_JPA_AUDITING);
        if (!enableAuditing) return null;

        return KotlinAttributeModel.builder()
                .name("updatedAt")
                .baseKotlinType("java.time.LocalDateTime")
                .isNullable(true)
                .annotations(List.of("@LastModifiedDate", "@Column(nullable = false)"))
                .initializer("null")
                .build();
    }

    default Set<String> collectEntityImportsKt(ProjectConfiguration config, EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("jakarta.persistence.*");

        boolean enableAuditing = NamingUtils.checkProjectOption(config, ProjectOptions.ENABLE_JPA_AUDITING);
        if (enableAuditing) {
            imports.add("org.springframework.data.annotation.CreatedDate");
            imports.add("org.springframework.data.annotation.LastModifiedDate");
            imports.add("org.springframework.data.jpa.domain.support.AuditingEntityListener");
            imports.add("java.time.LocalDateTime");
        }
        boolean hasRequiredFields = entity.getAttributes().stream().anyMatch(AttributeDefinition::isRequired);
        if (hasRequiredFields) {
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
        }
        boolean hasToManyRelationship = false;
        if (entity.getRelationships() != null) {
            hasToManyRelationship = entity.getRelationships().stream()
                    .anyMatch(r -> r.getRelationshipType() == RelationshipType.ONE_TO_MANY || r.getRelationshipType() == RelationshipType.MANY_TO_MANY);
        }
        if (!hasToManyRelationship && config.getEntities() != null) {
            for (EntityDefinition otherEntity : config.getEntities()) {
                if (otherEntity.getRelationships() == null) continue;
                for (RelationshipDefinition rel : otherEntity.getRelationships()) {
                    if (rel.getTargetEntity() != null && rel.getTargetEntity().getEntityName().equals(entity.getEntityName()) &&
                            (rel.getRelationshipType() == RelationshipType.MANY_TO_ONE)) {
                        hasToManyRelationship = true;
                        break;
                    }
                    if (rel.getTargetEntity() != null && rel.getTargetEntity().getEntityName().equals(entity.getEntityName()) &&
                            (rel.getRelationshipType() == RelationshipType.MANY_TO_MANY)) {
                        hasToManyRelationship = true;
                        break;
                    }
                }
                if (hasToManyRelationship) break;
            }
        }


        if (hasToManyRelationship) {
            imports.add("kotlin.collections.MutableSet");
            imports.add("java.util.HashSet");
        }
        entity.getAttributes().forEach(attr -> addKotlinTypeImport(imports, mapBaseKotlinType(attr.getDataType())));

        return imports;
    }

    default String mapBaseKotlinType(String definitionType) {
        if (definitionType == null) return "Any";
        return switch (definitionType.toLowerCase()) {
            case "string", "text", "varchar" -> "String";
            case "integer", "int" -> "Int";
            case "long", "bigint" -> "Long";
            case "double" -> "Double";
            case "float" -> "Float";
            case "decimal", "numeric" -> "java.math.BigDecimal";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "java.time.LocalDate";
            case "timestamp", "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "uuid" -> "java.util.UUID";
            case "blob", "bytea" -> "ByteArray";
            default -> "String";
        };
    }

    default void addKotlinTypeImport(Set<String> imports, String baseKotlinType) {
        if (baseKotlinType.startsWith("java.time.") || baseKotlinType.startsWith("java.math.") || baseKotlinType.startsWith("java.util.UUID")) {
            imports.add(baseKotlinType);
        }
    }
}