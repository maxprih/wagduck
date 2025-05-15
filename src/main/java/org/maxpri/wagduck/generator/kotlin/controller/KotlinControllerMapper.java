package org.maxpri.wagduck.generator.kotlin.controller;

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
public interface KotlinControllerMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".controller\")")
    @Mapping(target = "controllerClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Controller\")")
    @Mapping(target = "baseRequestPath", source = "entity.entityName", qualifiedByName = "determineBasePath")
    @Mapping(target = "serviceClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Service\")")
    @Mapping(target = "serviceFieldName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Service\")")
    @Mapping(target = "requestDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"RequestDto\")")
    @Mapping(target = "responseDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"ResponseDto\")")
    @Mapping(target = "primaryKeyType", source = "entity.attributes", qualifiedByName = "getControllerPrimaryKeyKotlinType")
    @Mapping(target = "primaryKeyName", source = "entity.attributes", qualifiedByName = "getControllerPrimaryKeyName")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectControllerImports")
    KotlinControllerModel toKotlinControllerDefModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("toPascalCaseStatic")
    default String toPascalCaseStatic(String name) {
        return NamingUtils.toPascalCase(name);
    }

    @Named("determineBasePath")
    default String determineBasePath(String entityName) {
        return NamingUtils.toSnakeCase(entityName);
    }

    @Named("getControllerPrimaryKeyKotlinType")
    default String getControllerPrimaryKeyKotlinType(List<AttributeDefinition> attributes) {
        return attributes.stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .map(attr -> mapDbDataTypeToKotlinBaseType(attr.getDataType()))
                .orElseThrow(() -> new IllegalStateException("Entity must have a primary key attribute for controller generation."));
    }

    @Named("getControllerPrimaryKeyName")
    default String getControllerPrimaryKeyName(List<AttributeDefinition> attributes) {
        return attributes.stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .map(attr -> NamingUtils.toCamelCase(attr.getAttributeName()))
                .orElseThrow(() -> new IllegalStateException("Entity must have a primary key attribute name for controller generation."));
    }

    @Named("collectControllerImports")
    default Set<String> collectControllerImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("org.springframework.http.HttpStatus");
        imports.add("org.springframework.http.ResponseEntity");
        imports.add("org.springframework.web.bind.annotation.*");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "RequestDto");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "ResponseDto");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".service." + NamingUtils.toPascalCase(entity.getEntityName()) + "Service");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".exception.EntityNotFoundException");

        entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .ifPresent(pkAttr -> {
                    String pkKotlinType = mapDbDataTypeToKotlinBaseType(pkAttr.getDataType());
                    addPotentialKotlinTypeImport(imports, pkKotlinType);
                });

        return imports;
    }

    default String mapDbDataTypeToKotlinBaseType(String definitionType) {
        if (definitionType == null) return "Any";
        return switch (definitionType.toLowerCase()) {
            case "string", "text", "varchar" -> "String";
            case "integer", "int" -> "Int";
            case "long", "bigint" -> "Long";
            case "double" -> "Double";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "java.time.LocalDate";
            case "timestamp", "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "uuid" -> "java.util.UUID";
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
