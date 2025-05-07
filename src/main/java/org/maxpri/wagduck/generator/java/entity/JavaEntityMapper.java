package org.maxpri.wagduck.generator.java.entity;

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
public interface JavaEntityMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName())")
    @Mapping(target = "tableName", source = "entity", qualifiedByName = "determineTableName")
    @Mapping(target = "useLombok", constant = "true") // Assume true for now, could come from config.getEnabledOptions()
    @Mapping(target = "includeAuditing", constant = "true") // Assume true for now
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributes")
    @Mapping(target = "relationships", expression = "java(mapAllRelationships(config, entity))")
    @Mapping(target = "classAnnotations", source = "entity", qualifiedByName = "generateEntityClassAnnotations")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectEntityImports")
    @Mapping(target = "createdAtAttribute", source = "entity", qualifiedByName = "generateCreatedAtAttribute")
    @Mapping(target = "updatedAtAttribute", source = "entity", qualifiedByName = "generateUpdatedAtAttribute")
    JavaEntityModel toJavaEntityModel(ProjectConfiguration config, EntityDefinition entity);

    // --- Helper Methods ---

    @Named("determineTableName")
    default String determineTableName(EntityDefinition entity) {
        if (entity.getTableName() != null && !entity.getTableName().isBlank()) {
            return entity.getTableName();
        }
        return NamingUtils.toSnakeCase(entity.getEntityName());
    }

    @Named("mapAttributes")
    default List<JavaAttributeModel> mapAttributes(List<AttributeDefinition> attributes) {
        if (attributes == null) return Collections.emptyList();
        return attributes.stream()
                .map(this::mapSingleAttribute)
                .collect(Collectors.toList());
    }

    default JavaAttributeModel mapSingleAttribute(AttributeDefinition attr) {
        List<String> annotations = new ArrayList<>();
        String javaType = mapDataType(attr.getDataType());
        String attributeName = NamingUtils.toCamelCase(attr.getAttributeName());

        if (attr.isPrimaryKey()) {
            annotations.add("@Id");
            String strategy = "GenerationType.IDENTITY";
            if ("UUID".equals(javaType)) {
                 strategy = "GenerationType.UUID";
            }
            annotations.add(String.format("@GeneratedValue(strategy = %s)", strategy));
        }

        List<String> columnParams = new ArrayList<>();
        String dbColumnName = attr.getColumnName() != null ? attr.getColumnName() : NamingUtils.toSnakeCase(attributeName);
        if (!dbColumnName.equals(NamingUtils.toSnakeCase(attributeName))) { // Only add if different from default JPA naming
             columnParams.add(String.format("name = \"%s\"", dbColumnName));
        }
        if (attr.isRequired() && !attr.isPrimaryKey()) { // Nullable is true by default unless PK
            columnParams.add("nullable = false");
        }
        if (attr.isUnique()) {
            columnParams.add("unique = true");
        }
        // TODO: Add columnDefinition for complex types or defaults?

        // --- Validation Annotations (Optional) ---
        if (attr.isRequired()) {
            if ("String".equals(javaType)) {
                 annotations.add("@NotBlank"); // From jakarta.validation
            } else {
                 annotations.add("@NotNull"); // From jakarta.validation
            }
        }

        if (!columnParams.isEmpty()) {
            annotations.add(String.format("@Column(%s)", String.join(", ", columnParams)));
        }

        return JavaAttributeModel.builder()
                .name(attributeName)
                .type(javaType)
                .isPrimaryKey(attr.isPrimaryKey())
                .annotations(annotations)
                .build();
    }


    @Named("mapRelationships")
    default List<JavaRelationshipModel> mapRelationships(List<RelationshipDefinition> relationships) {
         if (relationships == null) return Collections.emptyList();
         return relationships.stream()
                 .map(this::mapSingleRelationship)
                 .collect(Collectors.toList());
    }

     default JavaRelationshipModel mapSingleRelationship(RelationshipDefinition rel) {
        String relationshipName = NamingUtils.toCamelCase(rel.getSourceFieldName());
        String targetClassName = NamingUtils.toPascalCase(rel.getTargetEntity().getEntityName());
        String annotation;
        String fieldType;
        boolean useSet = true; // Default to Set for collections

        switch (rel.getRelationshipType()) {
            case ONE_TO_ONE:
                fieldType = targetClassName;
                if (rel.isOwningSide()) {
                    String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                    annotation = String.format("@OneToOne(fetch = FetchType.%s)\n    @JoinColumn(name = \"%s\", unique = true)",
                            rel.getFetchType(), joinColumn);
                } else {
                    annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = FetchType.%s)",
                            NamingUtils.toCamelCase(rel.getTargetFieldName()), rel.getFetchType());
                }
                break;
            case MANY_TO_ONE:
                fieldType = targetClassName;
                String joinColumn = rel.getJoinColumnName() != null ? rel.getJoinColumnName() : NamingUtils.toSnakeCase(relationshipName) + "_id";
                 annotation = String.format("@ManyToOne(fetch = FetchType.%s)\n    @JoinColumn(name = \"%s\")",
                         rel.getFetchType(), joinColumn);
                break;
            case ONE_TO_MANY:
                 fieldType = String.format("Set<%s>", targetClassName); // Or List? Defaulting to Set
                 // mappedBy is mandatory for OneToMany from non-owning side
                 String mappedBy = NamingUtils.toCamelCase(rel.getTargetFieldName());
                 annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = FetchType.%s, orphanRemoval = true)",
                         mappedBy, rel.getFetchType());
                 break;
            case MANY_TO_MANY:
                fieldType = String.format("Set<%s>", targetClassName);
                if (rel.isOwningSide()) {
                    String joinTableName = rel.getJoinTableName() != null ? rel.getJoinTableName() : NamingUtils.toSnakeCase(rel.getSourceEntity().getEntityName()) + "_" + NamingUtils.toSnakeCase(targetClassName);
                    String sourceJoinCol = rel.getJoinTableSourceColumnName() != null ? rel.getJoinTableSourceColumnName() : NamingUtils.toSnakeCase(rel.getSourceEntity().getEntityName()) + "_id";
                    String targetJoinCol = rel.getJoinTableTargetColumnName() != null ? rel.getJoinTableTargetColumnName() : NamingUtils.toSnakeCase(rel.getTargetEntity().getEntityName()) + "_id";
                    annotation = String.format("@ManyToMany(fetch = FetchType.%s)\n    @JoinTable(name = \"%s\",\n               joinColumns = @JoinColumn(name = \"%s\"),\n               inverseJoinColumns = @JoinColumn(name = \"%s\"))",
                            rel.getFetchType(), joinTableName, sourceJoinCol, targetJoinCol);
                } else {
                    String mappedTarget = NamingUtils.toCamelCase(rel.getTargetFieldName());
                     annotation = String.format("@ManyToMany(mappedBy = \"%s\", fetch = FetchType.%s)", mappedTarget, rel.getFetchType());
                }
                break;
            default:
                 throw new IllegalArgumentException("Unsupported relationship type: " + rel.getRelationshipType());
        }


         return JavaRelationshipModel.builder()
                 .name(relationshipName)
                 .type(fieldType)
                 .annotations(List.of(annotation)) // Wrap annotation string in a list
                 .build();
     }

    // --- Annotation Generation Helpers ---

    @Named("generateEntityClassAnnotations")
    default List<String> generateEntityClassAnnotations(EntityDefinition entity) {
        List<String> annotations = new ArrayList<>();
        boolean useLombok = true; // Assume true
        boolean includeAuditing = true; // Assume true

        annotations.add("@Entity");
        annotations.add(String.format("@Table(name = \"%s\")", determineTableName(entity)));

        if (useLombok) {
            annotations.add("@Data"); // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
            // Or be more specific:
            // annotations.add("@Getter");
            // annotations.add("@Setter");
            // annotations.add("@ToString"); // Consider excluding lazy-loaded fields
            // annotations.add("@EqualsAndHashCode(of = \"id\")"); // Base on PK
            annotations.add("@Builder");
            annotations.add("@NoArgsConstructor");
            annotations.add("@AllArgsConstructor");
        }
        if (includeAuditing) {
            annotations.add("@EntityListeners(AuditingEntityListener.class)");
        }
        return annotations;
    }

    @Named("generateCreatedAtAttribute")
    default JavaAttributeModel generateCreatedAtAttribute(EntityDefinition entity) {
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(),
                ProjectOptions.ENABLE_JPA_AUDITING);
        if (!enableAuditing) return null;
        return JavaAttributeModel.builder()
                .name("createdAt")
                .type("LocalDateTime")
                .annotations(List.of("@CreatedDate", "@Column(nullable = false, updatable = false)"))
                .build();
    }

    @Named("generateUpdatedAtAttribute")
    default JavaAttributeModel generateUpdatedAtAttribute(EntityDefinition entity) {
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(),
                ProjectOptions.ENABLE_JPA_AUDITING);
        if (!enableAuditing) return null;
        return JavaAttributeModel.builder()
                .name("updatedAt")
                .type("LocalDateTime")
                .annotations(List.of("@LastModifiedDate", "@Column(nullable = false)"))
                .build();
    }

    @Named("collectEntityImports")
    default Set<String> collectEntityImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        boolean useLombok = true;
        boolean enableAuditing = NamingUtils.checkProjectOption(entity.getProjectConfiguration(),
                ProjectOptions.ENABLE_JPA_AUDITING);

        imports.add("jakarta.persistence.*");

        if (useLombok) {
             imports.add("lombok.*");
        }

        if (enableAuditing) {
             imports.add("org.springframework.data.annotation.CreatedDate");
             imports.add("org.springframework.data.annotation.LastModifiedDate");
             imports.add("org.springframework.data.jpa.domain.support.AuditingEntityListener");
             imports.add("java.time.LocalDateTime");
        }

        if (entity.getAttributes().stream().anyMatch(AttributeDefinition::isRequired)) {
             imports.add("jakarta.validation.constraints.NotNull");
             imports.add("jakarta.validation.constraints.NotBlank");
        }
        // TODO: Add imports for other validation annotations if needed (@Email etc.)


        // Collections for relationships
        boolean hasToMany = entity.getRelationships().stream()
                .anyMatch(r -> r.getRelationshipType() == RelationshipType.ONE_TO_MANY || r.getRelationshipType() == RelationshipType.MANY_TO_MANY);
        if (hasToMany) {
            imports.add("java.util.Set");
            imports.add("java.util.HashSet");
            // imports.add("java.util.List"); // If using List instead of Set
            // imports.add("java.util.ArrayList");
        }

        // Data types from attributes
        entity.getAttributes().forEach(attr -> addTypeImport(imports, mapDataType(attr.getDataType())));
        // PK type if UUID
        entity.getAttributes().stream().filter(AttributeDefinition::isPrimaryKey).findFirst()
                .ifPresent(pk -> {
                    if ("UUID".equals(mapDataType(pk.getDataType()))) imports.add("java.util.UUID");
                });

        // Types from relationships (target entities - assuming they are in the same package)
        // No explicit import needed if in the same package, otherwise add based on target entity's package

        return imports;
    }

    // В JavaEntityMapper.java

    default List<JavaRelationshipModel> mapAllRelationships(ProjectConfiguration config, EntityDefinition entity) {
        List<JavaRelationshipModel> result = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        // Owner side (исходящие)
        if (entity.getRelationships() != null) {
            for (JavaRelationshipModel rel : mapRelationships(entity.getRelationships())) {
                if (usedNames.add(rel.getName())) {
                    result.add(rel);
                }
            }
        }
        // Inverse side (входящие)
        List<EntityDefinition> entities = config.getEntities();
        for (EntityDefinition other : entities) {
            if (other == entity || other.getRelationships() == null) continue;
            for (RelationshipDefinition rel : other.getRelationships()) {
                if (rel.getTargetEntity() != null && rel.getTargetEntity().equals(entity)
                        && rel.getTargetFieldName() != null && !rel.getTargetFieldName().isBlank()) {
                    JavaRelationshipModel inverse = mapInverseRelationship(rel, other, entity);
                    if (usedNames.add(inverse.getName())) {
                        result.add(inverse);
                    }
                }
            }
        }
        return result;
    }

    default JavaRelationshipModel mapInverseRelationship(RelationshipDefinition rel, EntityDefinition source, EntityDefinition target) {
        String relationshipName = org.maxpri.wagduck.util.NamingUtils.toCamelCase(rel.getTargetFieldName());
        String sourceClassName = org.maxpri.wagduck.util.NamingUtils.toPascalCase(source.getEntityName());
        String annotation;
        String fieldType;

        System.out.println(relationshipName);
        System.out.println(sourceClassName);
        switch (rel.getRelationshipType()) {
            case ONE_TO_MANY:
                // Inverse для OneToMany — это ManyToOne
                fieldType = sourceClassName;
                annotation = String.format("@ManyToOne(fetch = FetchType.%s)\n    @JoinColumn(name = \"%s\")",
                        rel.getFetchType(), rel.getJoinColumnName() != null ? rel.getJoinColumnName() : org.maxpri.wagduck.util.NamingUtils.toSnakeCase(relationshipName) + "_id");
                break;
            case MANY_TO_ONE:
                // Inverse для ManyToOne — это OneToMany
                fieldType = String.format("Set<%s>", sourceClassName);
                annotation = String.format("@OneToMany(mappedBy = \"%s\", fetch = FetchType.%s, orphanRemoval = true)",
                        org.maxpri.wagduck.util.NamingUtils.toCamelCase(rel.getSourceFieldName()), rel.getFetchType());
                break;
            case ONE_TO_ONE:
                // Inverse для OneToOne
                fieldType = sourceClassName;
                annotation = String.format("@OneToOne(mappedBy = \"%s\", fetch = FetchType.%s)",
                        org.maxpri.wagduck.util.NamingUtils.toCamelCase(rel.getSourceFieldName()), rel.getFetchType());
                break;
            case MANY_TO_MANY:
                // Inverse для ManyToMany
                fieldType = String.format("Set<%s>", sourceClassName);
                annotation = String.format("@ManyToMany(mappedBy = \"%s\", fetch = FetchType.%s)",
                        org.maxpri.wagduck.util.NamingUtils.toCamelCase(rel.getSourceFieldName()), rel.getFetchType());
                break;
            default:
                throw new IllegalArgumentException("Unsupported relationship type: " + rel.getRelationshipType());
        }

        return JavaRelationshipModel.builder()
                .name(relationshipName)
                .type(fieldType)
                .annotations(List.of(annotation))
                .build();
    }

    default String mapDataType(String definitionType) {
        if (definitionType == null) return "Object";
        return switch (definitionType.toLowerCase()) {
            case "string", "text", "varchar" -> "String";
            case "integer", "int" -> "Integer";
            case "long", "bigint" -> "Long";
            case "double", "float" -> "Double";
            case "decimal", "numeric" -> "java.math.BigDecimal";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "java.time.LocalDate";
            case "timestamp", "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "uuid" -> "UUID";
            case "blob", "bytea" -> "byte[]";
            default -> "String";
        };
    }

    default void addTypeImport(Set<String> imports, String javaType) {
         if (javaType.startsWith("java.time.") || javaType.startsWith("java.math.")) {
             imports.add(javaType);
         } else if ("UUID".equals(javaType)) {
             imports.add("java.util.UUID");
         }
     }
}