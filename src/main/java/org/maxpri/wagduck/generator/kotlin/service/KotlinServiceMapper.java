package org.maxpri.wagduck.generator.kotlin.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface KotlinServiceMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".service\")")
    @Mapping(target = "serviceClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Service\")")
    @Mapping(target = "entityClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "requestDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"RequestDto\")")
    @Mapping(target = "responseDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"ResponseDto\")")
    @Mapping(target = "mapperInterfaceName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Mapper\")")
    @Mapping(target = "mapperFieldName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Mapper\")")
    @Mapping(target = "repositoryInterfaceName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Repository\")")
    @Mapping(target = "repositoryFieldName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Repository\")")
    @Mapping(target = "primaryKeyType", source = "entity.attributes", qualifiedByName = "getPrimaryKeyKotlinType")
    @Mapping(target = "primaryKeyName", source = "entity.attributes", qualifiedByName = "getPrimaryKeyName")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectServiceImports")
    @Mapping(target = "entityNotFoundExceptionName", expression = "java(\"EntityNotFoundException\")")
    KotlinServiceModel toKotlinServiceDefModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("getPrimaryKeyKotlinType")
    default String getPrimaryKeyKotlinType(List<AttributeDefinition> attributes) {
        return attributes.stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .map(attr -> mapDbDataTypeToKotlinBaseType(attr.getDataType())) // Use shared utility or local method
                .orElseThrow(() -> new IllegalStateException("Entity must have a primary key attribute for service generation."));
    }

    @Named("getPrimaryKeyName")
    default String getPrimaryKeyName(List<AttributeDefinition> attributes) {
        return attributes.stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .map(attr -> NamingUtils.toCamelCase(attr.getAttributeName()))
                .orElseThrow(() -> new IllegalStateException("Entity must have a primary key attribute name for service generation."));
    }

    @Named("collectServiceImports")
    default Set<String> collectServiceImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("org.springframework.stereotype.Service");
        imports.add("org.springframework.transaction.annotation.Transactional"); // Common for service methods

        // Entity, DTOs, Mapper, Repository imports (already covered by direct mappings in KotlinServiceDefModel)
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".domain.model." + NamingUtils.toPascalCase(entity.getEntityName()));
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "RequestDto");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "ResponseDto");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".mapper." + NamingUtils.toPascalCase(entity.getEntityName()) + "Mapper");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".repository." + NamingUtils.toPascalCase(entity.getEntityName()) + "Repository");

        // Exception import
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".exception.EntityNotFoundException");

        // PK Type import if needed
        entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .ifPresent(pkAttr -> {
                    String pkKotlinType = mapDbDataTypeToKotlinBaseType(pkAttr.getDataType());
                    addPotentialKotlinTypeImport(imports, pkKotlinType);
                });

        return imports;
    }

    // --- Utility methods (ideally refactor to a shared class) ---
    default String mapDbDataTypeToKotlinBaseType(String definitionType) {
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

    default void addPotentialKotlinTypeImport(Set<String> imports, String baseKotlinType) {
        if (baseKotlinType.startsWith("java.time.") ||
            baseKotlinType.startsWith("java.math.") ||
            baseKotlinType.equals("java.util.UUID")) {
            imports.add(baseKotlinType);
        }
    }
}