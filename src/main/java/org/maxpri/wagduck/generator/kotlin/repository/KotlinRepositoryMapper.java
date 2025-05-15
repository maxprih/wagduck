package org.maxpri.wagduck.generator.kotlin.repository;

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
public interface KotlinRepositoryMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".repository\")")
    @Mapping(target = "repositoryName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Repository\")")
    @Mapping(target = "entityClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "entityClassImport", expression = "java(config.getBasePackage() + \".domain.model.\" + org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "primaryKeyType", source = "entity.attributes", qualifiedByName = "determinePrimaryKeyKotlinType")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectRepositoryImports")
    KotlinRepositoryModel toKotlinRepositoryModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("determinePrimaryKeyKotlinType")
    default String determinePrimaryKeyKotlinType(List<AttributeDefinition> attributes) {
        return attributes.stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .findFirst()
                .map(attr -> mapDbDataTypeToKotlinBaseType(attr.getDataType()))
                .orElseThrow(() -> new IllegalStateException("Entity must have a primary key attribute."));
    }

    @Named("collectRepositoryImports")
    default Set<String> collectRepositoryImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("org.springframework.data.jpa.repository.JpaRepository");
        imports.add("org.springframework.stereotype.Repository");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".domain.model." + NamingUtils.toPascalCase(entity.getEntityName()));
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