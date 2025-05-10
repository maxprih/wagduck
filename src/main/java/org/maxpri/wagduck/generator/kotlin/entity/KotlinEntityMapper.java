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
import org.maxpri.wagduck.util.NamingUtils; // Assuming you have this

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

    // --- Helper Methods ---

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
        String initializer = "null"; // Default for nullable types

        if (attr.isPrimaryKey()) {
            annotations.add("@Id");
            isNullable = true; // Primary keys are often nullable before persistence
            String strategy = "GenerationType.IDENTITY";
            if ("java.util.UUID".equals(baseKotlinType)) {
                strategy = "GenerationType.UUID";
            }
            annotations.add(String.format("@GeneratedValue(strategy = %s)", strategy));
        } else {
            if (!isNullable) { // Non-nullable and not PK
                 // For basic types, Kotlin requires initialization.
                 // JPA populates them, so we might rely on 'lateinit' or constructor,
                 // but for simplicity, let's assume they become nullable in the Kotlin class if not PK,
                 // and DB constraint + validation handles non-nullability.
                 // Alternatively, we can set them to a default value if truly non-nullable.
                 // For now, `isNullable = !attr.isRequired()` stands, and DB constraints are key.
                 // If a non-nullable field needs an initializer, it should be set here.
                 // e.g. if (baseKotlinType.equals("Boolean") && !isNullable) initializer = "false";
                 // This part might need refinement based on your exact needs for non-nullable fields.
                 // For now, if !attr.isRequired(), it maps to a non-nullable Kotlin type,
                 // which means it needs 'lateinit' or constructor init, or a default value.
                 // To keep it simple and work with JPA's no-arg constructor, let's make most fields nullable in Kotlin class.
                 // The `@Column(nullable=false)` will enforce DB constraint.
                 isNullable = true; // Make it nullable in Kotlin, let DB and validation handle it
                 initializer = "null";
            }
        }


        List<String> columnParams = new ArrayList<>();
        String dbColumnName = attr.getColumnName() != null ? attr.getColumnName() : NamingUtils.toSnakeCase(attributeName);
        // Only add 'name' attribute to @Column if it's different from the JPA default (snake_case of field name)
        if (!dbColumnName.equals(NamingUtils.toSnakeCase(attributeName))) {
            columnParams.add(String.format("name = \"%s\"", dbColumnName));
        }

        if (attr.isRequired() && !attr.isPrimaryKey()) { // For non-PK fields
            columnParams.add("nullable = false");
        }
        if (attr.isUnique()) {
            columnParams.add("unique = true");
        }
        // TODO: Add columnDefinition, length, precision, scale as needed

        // --- Validation Annotations (Optional) ---
        if (attr.isRequired()) {
            if ("String".equals(baseKotlinType)) {
                annotations.add("@field:NotBlank"); // Ensure it targets the field for Kotlin
            } else {
                annotations.add("@field:NotNull");
            }
        }
        // Add other validation annotations like @Size, @Email etc. as needed.
        // Example: if (attr.getMaxLength() != null) annotations.add(String.format("@field:Size(max = %d)", attr.getMaxLength()));


        if (!columnParams.isEmpty()) {
            annotations.add(String.format("@Column(%s)", String.join(", ", columnParams)));
        }
        
        // For primary key, initializer is usually null
        if (attr.isPrimaryKey()) {
            initializer = "null";
        }


        return KotlinAttributeModel.builder()
                .name(attributeName)
                .baseKotlinType(baseKotlinType)
                .isNullable(isNullable) // Controls the '?' in template
                .isPrimaryKey(attr.isPrimaryKey())
                .annotations(annotations)
                .initializer(initializer)
                .build();
    }

    default List<KotlinRelationshipModel> mapAllRelationshipsKt(ProjectConfiguration config, EntityDefinition currentEntity) {
        List<KotlinRelationshipModel> result = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        // Owning side relationships (defined in currentEntity)
        if (currentEntity.getRelationships() != null) {
            for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
                 // Ensure we pass currentEntity as the source for mapSingleRelationshipKt
                KotlinRelationshipModel relModel = mapSingleRelationshipKt(relDef, currentEntity, relDef.getTargetEntity());
                if (usedNames.add(relModel.getName())) {
                    result.add(relModel);
                }
            }
        }

        // Inverse side relationships (where currentEntity is the target)
        List<EntityDefinition> allEntities = config.getEntities();
        for (EntityDefinition otherEntity : allEntities) {
            if (otherEntity.equals(currentEntity) || otherEntity.getRelationships() == null) {
                continue;
            }
            for (RelationshipDefinition relDefOnOther : otherEntity.getRelationships()) {
                // Check if the target of this relationship is the currentEntity
                // and if an inverse field name is specified (targetFieldName)
                if (relDefOnOther.getTargetEntity() != null &&
                    relDefOnOther.getTargetEntity().getEntityName().equals(currentEntity.getEntityName()) && // Compare by name or actual object reference
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
        boolean isFieldNullable = true; // Default for ToOne, ToMany are usually non-null initialized

        String fetchTypeStr = "FetchType." + rel.getFetchType().name(); // e.g., FetchType.LAZY

        switch (rel.getRelationshipType()) {
            case ONE_TO_ONE:
                fieldBaseType = targetClassName;
                if (rel.isOwningSide()) {
                    String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                    annotation = String.format("@OneToOne(fetch = %s)\n    @JoinColumn(name = \"%s\", unique = true)",
                            fetchTypeStr, joinColumn);
                    // isFieldNullable depends on if the relationship is mandatory. Defaulting to true (nullable).
                } else {
                    // This is the non-owning side of a OneToOne defined by `targetFieldName` on the other side.
                    // `rel.getTargetFieldName()` should point to the field on `targetEntity` that maps this.
                    String mappedBy = NamingUtils.toCamelCase(rel.getTargetFieldName());
                     annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = %s)", mappedBy, fetchTypeStr);
                }
                break;
            case MANY_TO_ONE:
                fieldBaseType = targetClassName;
                String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                annotation = String.format("@ManyToOne(fetch = %s)\n    @JoinColumn(name = \"%s\")",
                        fetchTypeStr, joinColumn);
                // isFieldNullable depends on if the relationship is mandatory. Defaulting to true.
                break;
            case ONE_TO_MANY:
                fieldBaseType = String.format("MutableSet<%s>", targetClassName);
                String mappedByOneToMany = NamingUtils.toCamelCase(rel.getTargetFieldName()); // Field on targetEntity that owns this
                annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = %s, orphanRemoval = true, cascade = [CascadeType.ALL])", // Added cascade for common use case
                        mappedByOneToMany, fetchTypeStr);
                initializer = "mutableSetOf()";
                isFieldNullable = false; // Collections are usually initialized
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
                    String mappedByManyToMany = NamingUtils.toCamelCase(rel.getTargetFieldName()); // Field on targetEntity that owns this
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
        // originalRel is the relationship defined on originalSourceEntity, pointing to currentEntityAsTarget.
        // We are now defining the field on currentEntityAsTarget that is the other side of this originalRel.
        String inverseFieldName = NamingUtils.toCamelCase(originalRel.getTargetFieldName()); // This is the name of the field in currentEntityAsTarget
        String originalSourceClassName = NamingUtils.toPascalCase(originalSourceEntity.getEntityName());
        String annotation;
        String fieldBaseType;
        String initializer = "null";
        boolean isFieldNullable = true;

        String fetchTypeStr = "FetchType." + originalRel.getFetchType().name();

        switch (originalRel.getRelationshipType()) {
            case ONE_TO_MANY: // Original was OneToMany (sourceEntity has many currentEntities) -> Inverse is ManyToOne
                fieldBaseType = originalSourceClassName; // The type of the field in currentEntity is originalSourceEntity
                // The JoinColumn is on the 'many' side, which was originalSourceEntity in the context of originalRel.
                // Here, currentEntityAsTarget is the 'one' side. The 'many' side (originalSourceEntity) has the foreign key.
                // The 'mappedBy' for the original OneToMany was originalRel.getSourceFieldName().
                // So, the JoinColumn for this ManyToOne should correspond to how originalRel was defined.
                String joinColumnNameForManyToOne = originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(originalRel.getSourceFieldName()) + "_id";
                 // This assumes originalRel.getSourceFieldName() was the name of the field on the 'Many' side (originalSourceEntity) pointing to the 'One' (currentEntityAsTarget)
                // If originalRel.getTargetFieldName() was actually defining the join column for the ManyToOne side.
                // Let's re-evaluate this. The originalRel.getTargetFieldName() is the field on currentEntityAsTarget.
                // The originalRel.getSourceFieldName() is the field on originalSourceEntity.

                // If originalRel is: Source.targetFieldName (OneToMany) -> Target.sourceFieldName (ManyToOne)
                // We are generating Target.sourceFieldName. It will be ManyToOne.
                // The join column is typically on the table of the entity that has the ManyToOne.
                // So, the JoinColumn is on currentEntityAsTarget's table if originalRel.isOwningSide() was false for the collection,
                // or if currentEntityAsTarget has the FK.
                // For a OneToMany on originalSourceEntity mappedBy a field on currentEntityAsTarget,
                // currentEntityAsTarget has the ManyToOne and the JoinColumn.
                 String mappedByFieldOnOriginalSource = NamingUtils.toCamelCase(originalRel.getSourceFieldName()); // This is the collection field on originalSourceEntity
                 annotation = String.format("@ManyToOne(fetch = %s)\n    @JoinColumn(name = \"%s\")", // Name of FK col in currentEntityAsTarget's table
                        fetchTypeStr,
                        originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(inverseFieldName) + "_id" // Convention for FK column name
                 );
                break;

            case MANY_TO_ONE: // Original was ManyToOne (sourceEntity has one currentEntity) -> Inverse is OneToMany
                fieldBaseType = String.format("MutableSet<%s>", originalSourceClassName);
                // The 'mappedBy' refers to the field on originalSourceEntity that holds the ManyToOne relationship
                String mappedByFieldManyToOne = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = %s, orphanRemoval = true, cascade = [CascadeType.ALL])",
                        mappedByFieldManyToOne, fetchTypeStr);
                initializer = "mutableSetOf()";
                isFieldNullable = false;
                break;

            case ONE_TO_ONE: // Original was OneToOne
                fieldBaseType = originalSourceClassName;
                // The 'mappedBy' refers to the field on originalSourceEntity
                String mappedByOneToOne = NamingUtils.toCamelCase(originalRel.getSourceFieldName());
                if (originalRel.isOwningSide()) { // If original source owned it (had JoinColumn)
                    annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = %s)", mappedByOneToOne, fetchTypeStr);
                } else { // If original source was mappedBy (currentEntityAsTarget owned it)
                    // This case means currentEntityAsTarget has the @JoinColumn. So this inverse is the owning side.
                    String joinColumnNameO2O = originalRel.getJoinColumnName() != null ? originalRel.getJoinColumnName() : NamingUtils.toSnakeCase(mappedByOneToOne) + "_id"; // Or NamingUtils.toSnakeCase(inverseFieldName)
                     annotation = String.format("@OneToOne(fetch = %s)\n    @JoinColumn(name = \"%s\", unique = true)",
                            fetchTypeStr, joinColumnNameO2O);
                }
                break;

            case MANY_TO_MANY: // Original was ManyToMany
                fieldBaseType = String.format("MutableSet<%s>", originalSourceClassName);
                // The 'mappedBy' refers to the collection field on originalSourceEntity
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
        // You can add other class-level annotations here if needed
        // e.g. @DynamicUpdate, @DynamicInsert from Hibernate
        return annotations;
    }

    @Named("generateCreatedAtAttributeKt")
    default KotlinAttributeModel generateCreatedAtAttributeKt(EntityDefinition entity) {
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(), ProjectOptions.ENABLE_JPA_AUDITING);
        if (!enableAuditing) return null;

        return KotlinAttributeModel.builder()
                .name("createdAt")
                .baseKotlinType("java.time.LocalDateTime")
                .isNullable(true) // Typically nullable, Spring Data JPA handles it
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
                .isNullable(true) // Typically nullable
                .annotations(List.of("@LastModifiedDate", "@Column(nullable = false)"))
                .initializer("null")
                .build();
    }

    default Set<String> collectEntityImportsKt(ProjectConfiguration config, EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("jakarta.persistence.*"); // Covers Entity, Table, Id, GeneratedValue, Column, Enums like FetchType, CascadeType etc.
                                            // JoinColumn, JoinTable, OneToMany etc.

        boolean enableAuditing = NamingUtils.checkProjectOption(config, ProjectOptions.ENABLE_JPA_AUDITING);
        if (enableAuditing) {
            imports.add("org.springframework.data.annotation.CreatedDate");
            imports.add("org.springframework.data.annotation.LastModifiedDate");
            imports.add("org.springframework.data.jpa.domain.support.AuditingEntityListener");
            imports.add("java.time.LocalDateTime");
        }

        // Validation imports
        boolean hasRequiredFields = entity.getAttributes().stream().anyMatch(AttributeDefinition::isRequired);
        if (hasRequiredFields) {
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
            // Add other validation imports if you use them e.g. jakarta.validation.constraints.Size
        }

        // Collection types for relationships
        boolean hasToManyRelationship = false;
        if (entity.getRelationships() != null) {
             hasToManyRelationship = entity.getRelationships().stream()
                .anyMatch(r -> r.getRelationshipType() == RelationshipType.ONE_TO_MANY || r.getRelationshipType() == RelationshipType.MANY_TO_MANY);
        }
        // Check inverse relationships as well
        if (!hasToManyRelationship && config.getEntities() != null) {
            for (EntityDefinition otherEntity : config.getEntities()) {
                if (otherEntity.getRelationships() == null) continue;
                for (RelationshipDefinition rel : otherEntity.getRelationships()) {
                    if (rel.getTargetEntity() != null && rel.getTargetEntity().getEntityName().equals(entity.getEntityName()) &&
                        (rel.getRelationshipType() == RelationshipType.MANY_TO_ONE /* makes current a OneToMany */)) {
                         hasToManyRelationship = true;
                         break;
                    }
                     if (rel.getTargetEntity() != null && rel.getTargetEntity().getEntityName().equals(entity.getEntityName()) &&
                        (rel.getRelationshipType() == RelationshipType.MANY_TO_MANY /* makes current a ManyToMany */)) {
                         hasToManyRelationship = true;
                         break;
                    }
                }
                if (hasToManyRelationship) break;
            }
        }


        if (hasToManyRelationship) {
            imports.add("kotlin.collections.MutableSet"); // For mutableSetOf()
            imports.add("java.util.HashSet"); // If you prefer new HashSet<>()
            // Or if you use List:
            // imports.add("kotlin.collections.MutableList");
            // imports.add("java.util.ArrayList");
        }

        // Data types from attributes
        entity.getAttributes().forEach(attr -> addKotlinTypeImport(imports, mapBaseKotlinType(attr.getDataType())));

        return imports;
    }

    default String mapBaseKotlinType(String definitionType) {
        if (definitionType == null) return "Any"; // Or String as a fallback
        return switch (definitionType.toLowerCase()) {
            case "string", "text", "varchar" -> "String";
            case "integer", "int" -> "Int";
            case "long", "bigint" -> "Long";
            case "double" -> "Double"; // For float, consider "Float"
            case "float" -> "Float";
            case "decimal", "numeric" -> "java.math.BigDecimal";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "java.time.LocalDate";
            case "timestamp", "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "uuid" -> "java.util.UUID";
            case "blob", "bytea" -> "ByteArray"; // Kotlin's ByteArray maps to byte[]
            default -> "String"; // Fallback
        };
    }

    default void addKotlinTypeImport(Set<String> imports, String baseKotlinType) {
        if (baseKotlinType.startsWith("java.time.") || baseKotlinType.startsWith("java.math.") || baseKotlinType.startsWith("java.util.UUID")) {
            imports.add(baseKotlinType);
        }
        // "ByteArray" is kotlin.ByteArray, usually no import needed if kotlin.* is implicit
        // Primitives (Int, Long, Double, Boolean, Float) don't need import.
        // String doesn't need import.
    }
}