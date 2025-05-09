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

    // Main mapping method
    @Mapping(target = "packageName", expression = "java(deriveGoPackageName(config, entity))")
    @Mapping(target = "structName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "tableName", source = "entity", qualifiedByName = "determineGoTableName")
    @Mapping(target = "embedGormModel", expression = "java(checkGoProjectOption(config, \"EMBED_GORM_MODEL\", true))") // Example option
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectGoImports")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapGoAttributes")
    @Mapping(target = "relationships", expression = "java(mapGoRelationships(config, entity))")
    @Mapping(target = "idField", source = "entity", qualifiedByName = "generateGoIdField")
    @Mapping(target = "createdAtField", expression = "java(generateGoTimestampField(config, \"CreatedAt\", \"autoCreateTime\"))")
    @Mapping(target = "updatedAtField", expression = "java(generateGoTimestampField(config, \"UpdatedAt\", \"autoUpdateTime\"))")
    GoEntityModel toGoEntityModel(ProjectConfiguration config, EntityDefinition entity);

    // --- Helper Methods for Mapping ---

    default String deriveGoPackageName(ProjectConfiguration config, EntityDefinition entity) {
        // Example: Use a specific Go module structure or a default like "model"
        // String goModuleBase = config.getOption("GO_MODULE_BASE_PATH"); // e.g., "github.com/maxprih/mygoproject"
        // return "model"; // Or construct a more complex path if needed
        return "model"; // Defaulting to "model" for entities
    }

    @Named("determineGoTableName")
    default String determineGoTableName(EntityDefinition entity) {
        if (entity.getTableName() != null && !entity.getTableName().isBlank()) {
            return entity.getTableName(); // Use user-defined if present
        }
        // GORM default is snake_case plural of struct name.
        // You can return an empty string to let GORM use its default, or enforce your own.
        return NamingUtils.toSnakeCase(NamingUtils.toSnakeCase(entity.getEntityName()));
    }

    @Named("mapGoAttributes")
    default List<GoFieldModel> mapGoAttributes(List<AttributeDefinition> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }

        ProjectConfiguration config = attributes.get(0).getEntityDefinition().getProjectConfiguration();

        boolean embedGormModel = checkGoProjectOption(config, "EMBED_GORM_MODEL", true); // Your example hardcodes this to false

        // Determine if dedicated fields are being generated, to avoid duplicating them from the attributes list.
        boolean generateIdFieldSeparately = !embedGormModel;
        boolean generateCreatedAtFieldSeparately = !embedGormModel && checkGoProjectOption(config, "ADD_CREATEDAT_FIELD", true); // Your example hardcodes this to true
        boolean generateUpdatedAtFieldSeparately = !embedGormModel && checkGoProjectOption(config, "ADD_UPDATEDAT_FIELD", true); // Your example hardcodes this to true

        return attributes.stream()
                .filter(attr -> {
                    // 1. Exclude the primary key attribute if a dedicated 'idField' is being generated.
                    if (generateIdFieldSeparately && attr.isPrimaryKey()) {
                        return false;
                    }

                    String attrNameLower = attr.getAttributeName().toLowerCase();
                    String colNameLower = attr.getColumnName() != null ? attr.getColumnName().toLowerCase() : "";

                    // 2. Exclude 'CreatedAt'-like attributes if a dedicated 'createdAtField' is being generated.
                    //    Match by common naming conventions.
                    if (generateCreatedAtFieldSeparately &&
                            (attrNameLower.equals("createdat") || attrNameLower.equals("created_at") || colNameLower.equals("created_at"))) {
                        return false;
                    }

                    // 3. Exclude 'UpdatedAt'-like attributes if a dedicated 'updatedAtField' is being generated.
                    if (generateUpdatedAtFieldSeparately &&
                            (attrNameLower.equals("updatedat") || attrNameLower.equals("updated_at") || colNameLower.equals("updated_at"))) {
                        return false;
                    }

                    // 4. If embedding gorm.Model, further exclude attributes that gorm.Model itself provides by convention.
                    //    (PK, CreatedAt, UpdatedAt would likely be caught by the conditions above or are part of gorm.Model).
                    if (embedGormModel) {
                        if (attr.isPrimaryKey() || // Should be caught by generateIdFieldSeparately logic if embedGormModel is false
                                colNameLower.equals("created_at") ||
                                colNameLower.equals("updated_at") ||
                                colNameLower.equals("deleted_at")) { // Check for DeletedAt as well
                            return false;
                        }
                    }
                    return true; // Keep the attribute if none of the above conditions were met.
                })
                .map(attr -> mapSingleGoAttribute(attr, attr.getEntityDefinition().getProjectConfiguration())) // Pass config from attr's entity
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
            else if ("uuid.UUID".equals(goType)) gormParts.add("type:uuid;default:gen_random_uuid()"); // For PostgreSQL
        }

        if (!columnName.equals(NamingUtils.toSnakeCase(fieldName.replaceAll("ID$", ""))) && !attr.isPrimaryKey()) { // GORM default column name is snake_case of field name
             gormParts.add("column:" + columnName);
        }

        if (attr.isRequired() && !attr.isPrimaryKey()) {
            gormParts.add("not null");
        }
        if (attr.isUnique()) {
            gormParts.add("unique");
        }
        // Add other constraints like precision, scale for decimals if needed

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


    // Replace your existing mapGoRelationships method with this
    default List<GoFieldModel> mapGoRelationships(ProjectConfiguration config, EntityDefinition currentEntity) {
        if (currentEntity.getRelationships() == null) return Collections.emptyList();
        List<GoFieldModel> goFields = new ArrayList<>();

        for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
            EntityDefinition targetEntityDef = findEntity(config, relDef.getTargetEntity().getEntityName());
            if (targetEntityDef == null) {
                System.err.println("Warning: Target entity " + relDef.getTargetEntity().getEntityName() + " not found for relationship in " + currentEntity.getEntityName());
                continue;
            }

            String sourceFieldName = NamingUtils.toPascalCase(relDef.getSourceFieldName()); // e.g., Profile (for User.Profile), Orders (for User.Orders)
            String targetStructName = NamingUtils.toPascalCase(targetEntityDef.getEntityName()); // e.g., Profile, Order

            // Builder for the association field (e.g., Profile *Profile, Orders []Order)
            GoFieldModel.GoFieldModelBuilder associationFieldBuilder = GoFieldModel.builder()
                    .name(sourceFieldName)
                    .jsonTag("json:\"" + NamingUtils.toCamelCase(relDef.getSourceFieldName()) + ",omitempty\"")
                    .isRelationship(true); // Mark this as the actual association field

            List<String> gormPartsAssociation = new ArrayList<>();
            GoFieldModel.GoFieldModelBuilder fkFieldBuilder = null; // Builder for the scalar FK field (e.g., ProfileID uint)

            String goFkFieldName = ""; // Will store the Go name for the FK field, e.g., "ProfileID", "CustomerID"

            switch (relDef.getRelationshipType()) {
                case ONE_TO_ONE:
                    associationFieldBuilder.type("*" + targetStructName).isPointer(true);
                    if (relDef.isOwningSide()) {
                        // Current entity has the foreign key, e.g., User has ProfileID (for User.Profile)
                        // The FK field is named after the source/association field + "ID"
                        goFkFieldName = sourceFieldName + "ID";

                        String dbFkColumnName = (relDef.getJoinColumnName() != null && !relDef.getJoinColumnName().isBlank())
                                ? relDef.getJoinColumnName()
                                : NamingUtils.toSnakeCase(goFkFieldName);

                        List<String> gormPartsFkField = new ArrayList<>();
                        gormPartsFkField.add("column:" + dbFkColumnName);
                        // Add other constraints like unique if specified in relDef for the FK
                        // if (relDef.isForeignKeyUnique()) gormPartsFkField.add("unique");

                        fkFieldBuilder = GoFieldModel.builder()
                                .name(goFkFieldName)
                                .type("*" + getPrimaryKeyGoType(targetEntityDef, config))
                                .jsonTag("json:\"" + NamingUtils.toCamelCase(goFkFieldName) + ",omitempty\"") // Or json:"-"
                                .gormTag("gorm:\"" + String.join(";", gormPartsFkField) + "\"")
                                .isRelationship(false); // This is a scalar FK field, not the association itself

                        gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                        // If the target's PK field name is not "ID", specify it with "references"
                        String targetPkFieldName = findPrimaryKeyFieldName(targetEntityDef);
                        if (!"ID".equals(targetPkFieldName)) { // GORM default PK name for gorm.Model is ID
                            gormPartsAssociation.add("references:" + targetPkFieldName);
                        }

                    } else { // Non-owning side
                        // FK is on the target entity. Tag refers to FK field on target.
                        // e.g., Profile has User *User, and User (target) has ProfileID.
                        // The FK field on the target (User) is typically CurrentEntityName + "ID"
                        goFkFieldName = NamingUtils.toPascalCase(currentEntity.getEntityName()) + "ID";
                        gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                        // References tag might be needed if current entity's PK (that FK on target refers to) is not 'ID'
                        String currentPkFieldName = findPrimaryKeyFieldName(currentEntity);
                        if (!"ID".equals(currentPkFieldName)) {
                            gormPartsAssociation.add("references:" + currentPkFieldName);
                        }
                    }
                    break;

                case MANY_TO_ONE: // Current entity (Many) has FK to Target (One)
                    associationFieldBuilder.type("*" + targetStructName).isPointer(true);

                    // FK field on current struct is named after TargetStructName + "ID", e.g., Order has CustomerID
                    goFkFieldName = targetStructName + "ID";
                    String dbFkColumnNameMTO = (relDef.getJoinColumnName() != null && !relDef.getJoinColumnName().isBlank())
                            ? relDef.getJoinColumnName()
                            : NamingUtils.toSnakeCase(goFkFieldName);

                    List<String> gormPartsFkFieldMTO = new ArrayList<>();
                    gormPartsFkFieldMTO.add("column:" + dbFkColumnNameMTO);
                    // Add 'not null' if relationship is mandatory based on relDef
                    // if (relDef.isRequired()) gormPartsFkFieldMTO.add("not null");

                    fkFieldBuilder = GoFieldModel.builder()
                            .name(goFkFieldName)
                            .type("*" + getPrimaryKeyGoType(targetEntityDef, config))
                            .jsonTag("json:\"" + NamingUtils.toCamelCase(goFkFieldName) + ",omitempty\"")
                            .gormTag("gorm:\"" + String.join(";", gormPartsFkFieldMTO) + "\"")
                            .isRelationship(false); // Scalar FK field

                    gormPartsAssociation.add("foreignKey:" + goFkFieldName); // e.g., User.Role *Role `gorm:"foreignKey:RoleID"`
                    String targetPkFieldNameMTO = findPrimaryKeyFieldName(targetEntityDef);
                    if (!"ID".equals(targetPkFieldNameMTO)) {
                        gormPartsAssociation.add("references:" + targetPkFieldNameMTO);
                    }
                    break;

                case ONE_TO_MANY: // Current entity (One) to Target (Many). FK is on Target.
                    associationFieldBuilder.type("[]" + targetStructName).isSlice(true);
                    // TargetStruct (Many) has a CurrentStructNameID field.
                    // e.g., User has Posts []Post `gorm:"foreignKey:UserID"` (UserID is on Post table/struct)
                    goFkFieldName = NamingUtils.toPascalCase(currentEntity.getEntityName()) + "ID";
                    gormPartsAssociation.add("foreignKey:" + goFkFieldName);
                    // References tag might be needed if current entity's PK (that FK on target refers to) is not 'ID'
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
                    // You can add ;joinForeignKey:YourSourceRefer;joinTargetForeignKey:YourTargetRefer if needed
                    break;
                default:
                    associationFieldBuilder.type("interface{}"); // Placeholder
            }

            // Add the scalar FK field first (if generated)
            if (fkFieldBuilder != null) {
                // TODO: Add a check here to prevent duplicating FK field if it's already defined as a regular attribute
                // This check would involve looking into `currentEntity.getAttributes()` and matching by expected FK name/column.
                // For now, we assume FKs are primarily defined via relationships.
                goFields.add(fkFieldBuilder.build());
            }

            // Then add the association field
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

    // Add this helper method to your GoEntityMapper interface
    default String getPrimaryKeyGoType(EntityDefinition entity, ProjectConfiguration config) {
        // Check if the target entity embeds gorm.Model
        if (checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true)) { // Use project config of the entity itself
            return "uint"; // gorm.Model's ID is uint
        }
        // Otherwise, find its explicitly defined primary key attribute
        AttributeDefinition pkAttribute = entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst().orElse(null);

        if (pkAttribute != null) {
            return mapToGoDataType(pkAttribute.getDataType());
        }
        // Fallback if no explicit PK and not embedding gorm.Model (though this case should be rare if entities are well-defined)
        return "uint";
    }

    @Named("collectGoImports")
    default Set<String> collectGoImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        boolean embedGorm = checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true);

        boolean hasTimeField = false;
        if (!embedGorm) { // If not embedding, we might manually add CreatedAt/UpdatedAt
            if (checkGoProjectOption(entity.getProjectConfiguration(), "ADD_CREATED_AT_FIELD", true) || checkGoProjectOption(entity.getProjectConfiguration(), "ADD_UPDATED_AT_FIELD", true)) {
                 hasTimeField = true;
            }
            // Check for custom ID of type UUID
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
        // Relationships might introduce types that need imports if they are from other generated packages,
        // but for now, assume they are in the same "model" package or standard types.

        if (hasTimeField) {
            imports.add("time");
        }

        // For relationships, if they point to types like uuid.UUID in foreign keys etc.
        // This part might need more sophisticated analysis based on GORM tag contents or related entity PK types.

        return imports;
    }


    @Named("generateGoIdField")
    default GoFieldModel generateGoIdField(EntityDefinition entity) {
        if (checkGoProjectOption(entity.getProjectConfiguration(), "EMBED_GORM_MODEL", true)) {
            return null; // Handled by gorm.Model
        }
        AttributeDefinition pkAttribute = entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst().orElse(null);

        if (pkAttribute == null) { // Create a default ID if no PK is defined and not embedding gorm.Model
            return GoFieldModel.builder()
                    .name("ID")
                    .type("uint") // Default GORM ID type
                    .gormTag("gorm:\"primaryKey;autoIncrement\"")
                    .jsonTag("json:\"id\"")
                    .description("Unique identifier for the record.")
                    .build();
        }

        String goType = mapToGoDataType(pkAttribute.getDataType());
        String fieldName = NamingUtils.toPascalCase(pkAttribute.getAttributeName());
        String gormTag = "gorm:\"primaryKey";
        if (isIntegerType(goType)) gormTag += ";autoIncrement";
        else if ("uuid.UUID".equals(goType)) gormTag += ";type:uuid;default:gen_random_uuid()"; // For PostgreSQL
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
        // Check if a project option explicitly disables this field
        if (!checkGoProjectOption(config, "ADD_" + fieldName.toUpperCase() + "_FIELD", true)) {
            return null;
        }

        return GoFieldModel.builder()
                .name(fieldName) // CreatedAt, UpdatedAt
                .type("time.Time")
                .gormTag("gorm:\"" + gormTimeFunction + "\"") // autoCreateTime, autoUpdateTime
                .jsonTag("json:\"" + NamingUtils.toCamelCase(fieldName) + "\"")
                .description(fieldName.equals("CreatedAt") ? "Timestamp of record creation." : "Timestamp of last record update.")
                .build();
    }

    default String mapToGoDataType(String javaDataType) {
        if (javaDataType == null) return "interface{}"; // any type
        return switch (javaDataType.toLowerCase()) {
            case "string", "text", "varchar" -> "string";
            case "integer", "int" -> "int"; // Or int34, int64 depending on size
            case "long", "bigint" -> "int64";
            case "double" -> "float64";
            case "float" -> "float32";
            case "decimal", "numeric" -> "decimal.Decimal"; // github.com/shopspring/decimal
            case "boolean", "bool" -> "bool";
            case "date", "timestamp", "datetime" -> "time.Time"; // GORM handles date vs datetime
            case "time" -> "time.Time"; // Consider custom type or string if only time part
            case "uuid" -> "uuid.UUID"; // github.com/google/uuid
            case "blob", "bytea" -> "[]byte";
            default -> "string"; // Default fallback
        };
    }

    // Helper to check a Go-specific project option
    default boolean checkGoProjectOption(ProjectConfiguration config, String optionKey, boolean defaultValue) {
        // You'll need to define how Go-specific options are stored in ProjectConfiguration
        // For example, config.getGoOptions().getOrDefault(optionKey, defaultValue);
        // Or adapt your existing ProjectOptions enum/mechanism.
        // This is a placeholder.
        if ("EMBED_GORM_MODEL".equals(optionKey)) return false; // Example default
        if (optionKey.startsWith("ADD_") && optionKey.endsWith("_FIELD")) return true; // Example default
        return defaultValue;
    }

     default EntityDefinition findEntity(ProjectConfiguration config, String entityName) {
        return config.getEntities().stream()
                .filter(e -> e.getEntityName().equalsIgnoreCase(entityName))
                .findFirst().orElse(null);
    }
}