package org.maxpri.wagduck.generator.java.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.java.entity.JavaAttributeModel;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaDtoMapper {

    // --- Request DTO Mapping ---
    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName() + \"Request\")")
    @Mapping(target = "description", expression = "java(\"DTO for creating/updating \" + entity.getEntityName() + \".\")")
    @Mapping(target = "useLombok", constant = "true")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributesForRequestDto")
    @Mapping(target = "imports", source = "entity.attributes", qualifiedByName = "collectRequestDtoImports")
    JavaDtoModel toRequestDtoModel(ProjectConfiguration config, EntityDefinition entity);

    // --- Response DTO Mapping ---
    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName() + \"Response\")")
    @Mapping(target = "description", expression = "java(\"DTO for representing \" + entity.getEntityName() + \".\")")
    @Mapping(target = "useLombok", constant = "true")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributesForResponseDto")
    @Mapping(target = "imports", source = "entity.attributes", qualifiedByName = "collectResponseDtoImports")
    JavaDtoModel toResponseDtoModel(ProjectConfiguration config, EntityDefinition entity);


    // --- Attribute Mapping Helpers ---

    @Named("mapAttributesForRequestDto")
    default List<JavaAttributeModel> mapAttributesForRequestDto(List<AttributeDefinition> attributes) {
        if (attributes == null) return Collections.emptyList();
        return attributes.stream()
                .filter(attr -> !attr.isPrimaryKey() && !isAuditField(attr.getAttributeName()))
                .map(attr -> {
                    JavaAttributeModel model = mapSingleAttributeForDto(attr);
                    addValidationAnnotations(model, attr);
                    return model;
                })
                .collect(Collectors.toList());
    }

    @Named("mapAttributesForResponseDto")
    default List<JavaAttributeModel> mapAttributesForResponseDto(List<AttributeDefinition> attributes) {
         if (attributes == null) return Collections.emptyList();
         return attributes.stream()
                 // Include primary key for response
                 // Exclude sensitive fields if needed (e.g., based on name or a custom flag)
                 // .filter(attr -> !isSensitive(attr.getAttributeName()))
                 .map(this::mapSingleAttributeForDto) // Use common DTO mapping
                 .collect(Collectors.toList());
    }

    // Common logic to map an attribute to its DTO representation (without validation)
    default JavaAttributeModel mapSingleAttributeForDto(AttributeDefinition attr) {
         String javaType = mapDataType(attr.getDataType()); // Reuse data type mapping logic
         String attributeName = NamingUtils.toCamelCase(attr.getAttributeName());

         // DTOs generally don't need JPA annotations
         List<String> annotations = new ArrayList<>();

         // Add description as JavaDoc comment later in the template

         return JavaAttributeModel.builder()
                 .name(attributeName)
                 .type(javaType)
                 .isPrimaryKey(attr.isPrimaryKey()) // Keep track if it's PK for response DTO
                 .annotations(annotations) // Start with empty annotations
                 .build();
    }

    // Adds validation annotations to a JavaAttributeModel (used for Request DTO)
    default void addValidationAnnotations(JavaAttributeModel model, AttributeDefinition attr) {
        // Need original AttributeDefinition to know requirements - This approach is limited.
        // A better way: Pass the original AttributeDefinition list to the collectImports/Mapper
        // For now, let's add basic NotNull/NotBlank based on common practice (assuming non-PK fields are required)
        // This is a *major simplification* - real validation should come from AttributeDefinition flags.

        // Placeholder: Assume all non-PK fields in Request DTO are required
        if (!model.isPrimaryKey()) {
             if (attr.isRequired()) {// Primary keys are usually excluded from request DTO anyway
                 if ("String".equals(model.getType())) {
                     model.getAnnotations().add("@NotBlank");
                 } else {
                     model.getAnnotations().add("@NotNull");
                 }
             }
        }
         // TODO: Add @Size, @Email, @Pattern etc. based on original AttributeDefinition properties
    }

    // --- Import Collection Helpers ---

    @Named("collectRequestDtoImports")
    default Set<String> collectRequestDtoImports(List<AttributeDefinition> attributes) {
        Set<String> imports = collectCommonDtoImports(attributes);
        // Add validation imports if any validation annotations were added
        if (attributes.stream().anyMatch(attr -> !attr.isPrimaryKey() && !isAuditField(attr.getAttributeName()))) { // Basic check if required fields exist
             imports.add("jakarta.validation.constraints.NotNull");
             imports.add("jakarta.validation.constraints.NotBlank");
             // TODO: Add imports for @Size, @Email etc. if used
        }
        return imports;
    }

    @Named("collectResponseDtoImports")
    default Set<String> collectResponseDtoImports(List<AttributeDefinition> attributes) {
        return collectCommonDtoImports(attributes); // Response DTO usually doesn't need validation imports
    }

    default Set<String> collectCommonDtoImports(List<AttributeDefinition> attributes) {
        Set<String> imports = new HashSet<>();
        boolean useLombok = true;

        if (useLombok) {
            imports.add("lombok.Data"); // Or Getter/Setter/etc.
            imports.add("lombok.Builder");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
        }

        // Add imports for attribute types
        if (attributes != null) {
            attributes.stream()
                 // Include PK for response, exclude for request (already filtered for request)
                 // Let's just add all potential types needed by either
                .forEach(attr -> addTypeImport(imports, mapDataType(attr.getDataType())));
        }

        return imports;
    }


    // --- Utility Methods (reuse from other mappers or centralize) ---

    default String mapDataType(String definitionType) {
        // Copied from JavaEntityMapper - Centralize this logic!
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
         // Copied from JavaEntityMapper - Centralize this logic!
         if (javaType.startsWith("java.time.") || javaType.startsWith("java.math.")) {
             imports.add(javaType);
         } else if ("UUID".equals(javaType)) {
             imports.add("java.util.UUID");
         }
     }

     default boolean isAuditField(String attributeName) {
         // Simple check for common audit field names
         String lowerName = attributeName.toLowerCase();
         return lowerName.equals("createdat") || lowerName.equals("updatedat") ||
                lowerName.equals("createdby") || lowerName.equals("updatedby");
     }

     // Placeholder for sensitive field check (e.g., password)
     // default boolean isSensitive(String attributeName) {
     //    return attributeName.toLowerCase().contains("password");
     // }
}