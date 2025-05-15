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

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName() + \"Request\")")
    @Mapping(target = "description", expression = "java(\"DTO for creating/updating \" + entity.getEntityName() + \".\")")
    @Mapping(target = "useLombok", constant = "true")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributesForRequestDto")
    @Mapping(target = "imports", source = "entity.attributes", qualifiedByName = "collectRequestDtoImports")
    JavaDtoModel toRequestDtoModel(ProjectConfiguration config, EntityDefinition entity);

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "className", expression = "java(entity.getEntityName() + \"Response\")")
    @Mapping(target = "description", expression = "java(\"DTO for representing \" + entity.getEntityName() + \".\")")
    @Mapping(target = "useLombok", constant = "true")
    @Mapping(target = "attributes", source = "entity.attributes", qualifiedByName = "mapAttributesForResponseDto")
    @Mapping(target = "imports", source = "entity.attributes", qualifiedByName = "collectResponseDtoImports")
    JavaDtoModel toResponseDtoModel(ProjectConfiguration config, EntityDefinition entity);

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
                .map(this::mapSingleAttributeForDto)
                .collect(Collectors.toList());
    }

    default JavaAttributeModel mapSingleAttributeForDto(AttributeDefinition attr) {
        String javaType = mapDataType(attr.getDataType());
        String attributeName = NamingUtils.toCamelCase(attr.getAttributeName());
        List<String> annotations = new ArrayList<>();

        return JavaAttributeModel.builder()
                .name(attributeName)
                .type(javaType)
                .isPrimaryKey(attr.isPrimaryKey())
                .annotations(annotations)
                .build();
    }

    default void addValidationAnnotations(JavaAttributeModel model, AttributeDefinition attr) {
        if (!model.isPrimaryKey()) {
            if (attr.isRequired()) {
                if ("String".equals(model.getType())) {
                    model.getAnnotations().add("@NotBlank");
                } else {
                    model.getAnnotations().add("@NotNull");
                }
            }
        }
    }

    @Named("collectRequestDtoImports")
    default Set<String> collectRequestDtoImports(List<AttributeDefinition> attributes) {
        Set<String> imports = collectCommonDtoImports(attributes);
        if (attributes.stream().anyMatch(attr -> !attr.isPrimaryKey() && !isAuditField(attr.getAttributeName()))) {
            imports.add("jakarta.validation.constraints.NotNull");
            imports.add("jakarta.validation.constraints.NotBlank");
        }
        return imports;
    }

    @Named("collectResponseDtoImports")
    default Set<String> collectResponseDtoImports(List<AttributeDefinition> attributes) {
        return collectCommonDtoImports(attributes);
    }

    default Set<String> collectCommonDtoImports(List<AttributeDefinition> attributes) {
        Set<String> imports = new HashSet<>();
        boolean useLombok = true;

        if (useLombok) {
            imports.add("lombok.Data");
            imports.add("lombok.Builder");
            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
        }
        if (attributes != null) {
            attributes.stream()
                    .forEach(attr -> addTypeImport(imports, mapDataType(attr.getDataType())));
        }

        return imports;
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

    default boolean isAuditField(String attributeName) {
        String lowerName = attributeName.toLowerCase();
        return lowerName.equals("createdat") || lowerName.equals("updatedat") ||
                lowerName.equals("createdby") || lowerName.equals("updatedby");
    }
}