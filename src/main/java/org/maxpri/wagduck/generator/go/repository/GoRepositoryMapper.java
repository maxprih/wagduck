package org.maxpri.wagduck.generator.go.repository;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface GoRepositoryMapper {

    @Mapping(target = "packageName", expression = "java(deriveGoRepositoryPackage(config))")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Repository\")")
    @Mapping(target = "entityName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackageName", expression = "java(deriveGoModelsPackage(config))")
    @Mapping(target = "entityStructName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityIdType", source = "entity", qualifiedByName = "mapEntityIdType")
    @Mapping(target = "entityIdParameterName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"ID\")")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectInterfaceImports")
    @Mapping(target = "methods", source = "entity", qualifiedByName = "generateCRUDMethodsForInterface")
    @Mapping(target = "description", expression = "java(entity.getEntityName() + \"Repository defines the interface for \" + entity.getEntityName().toLowerCase() + \" data operations.\")")
    GoRepositoryInterfaceModel mapToInterfaceModel(EntityDefinition entity, @Context ProjectConfiguration config);

    @Mapping(target = "packageName", expression = "java(deriveGoRepositoryPackage(config))")
    @Mapping(target = "structName", expression = "java(\"gorm\" + entity.getEntityName() + \"Repository\")")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Repository\")")
    @Mapping(target = "receiverName", expression = "java(String.valueOf(entity.getEntityName().toLowerCase().charAt(0)) + \"r\")")
    @Mapping(target = "entityName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackageName", expression = "java(deriveGoModelsPackage(config))")
    @Mapping(target = "entityStructName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityIdType", source = "entity", qualifiedByName = "mapEntityIdType")
    @Mapping(target = "entityIdParameterName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"ID\")")
    @Mapping(target = "entityIdStructField", source = "entity", qualifiedByName = "mapEntityIdStructField")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectImplementationImports")
    @Mapping(target = "methods", source = "entity", qualifiedByName = "generateCRUDMethodsForImplementation")
    @Mapping(target = "description", expression = "java(\"gorm\" + entity.getEntityName() + \"Repository implements \" + entity.getEntityName() + \"Repository using GORM.\")")
    GoRepositoryImplementationModel mapToImplementationModel(EntityDefinition entity, @Context ProjectConfiguration config);

    default String deriveGoRepositoryPackage(@Context ProjectConfiguration config) {
        return "repository";
    }

    default String deriveGoModelsPackage(@Context ProjectConfiguration config) {
        return "model";
    }

    default String getGoModulePath(@Context ProjectConfiguration config) {
        return config.getModuleName();
    }

    @Named("mapEntityIdType")
    default String mapEntityIdType(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .map(attr -> mapToGoDataType(attr.getDataType()))
                .findFirst()
                .orElse("uint");
    }

    @Named("mapEntityIdStructField")
    default String mapEntityIdStructField(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .map(attr -> NamingUtils.toPascalCase(attr.getAttributeName()))
                .findFirst()
                .orElse("ID");
    }

    default String mapToGoDataType(String definitionType) {
        if (definitionType == null) return "interface{}";
        return switch (definitionType.toLowerCase()) {
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

    @Named("collectInterfaceImports")
    default Set<String> collectInterfaceImports(EntityDefinition entity, @Context ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        imports.add("context");
        imports.add(getGoModulePath(config) + "/" + deriveGoModelsPackage(config));
        String idType = mapEntityIdType(entity);
        if ("uuid.UUID".equals(idType)) {
            imports.add("github.com/google/uuid");
        }
        if ("time.Time".equals(idType) || "*time.Time".equals(idType)) {
            imports.add("time");
        }
        if ("decimal.Decimal".equals(idType) || "*decimal.Decimal".equals(idType)) {
            imports.add("github.com/shopspring/decimal");
        }
        return imports;
    }

    @Named("collectImplementationImports")
    default Set<String> collectImplementationImports(EntityDefinition entity, @Context ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        imports.add("context");
        imports.add("gorm.io/gorm");
        imports.add("errors");
        imports.add(getGoModulePath(config) + "/" + deriveGoModelsPackage(config));

        String idType = mapEntityIdType(entity);
        if ("uuid.UUID".equals(idType)) {
            imports.add("github.com/google/uuid");
        }
        if ("time.Time".equals(idType) || "*time.Time".equals(idType)) {
            imports.add("time");
        }
        if ("decimal.Decimal".equals(idType) || "*decimal.Decimal".equals(idType)) {
            imports.add("github.com/shopspring/decimal");
        }
        return imports;
    }

    @Named("generateCRUDMethodsForInterface")
    default List<GoRepositoryMethodModel> generateCRUDMethodsForInterface(EntityDefinition entity, @Context ProjectConfiguration config) {
        return generateCRUDMethods(entity, config, false);
    }

    @Named("generateCRUDMethodsForImplementation")
    default List<GoRepositoryMethodModel> generateCRUDMethodsForImplementation(EntityDefinition entity, @Context ProjectConfiguration config) {
        return generateCRUDMethods(entity, config, true);
    }

    default List<GoRepositoryMethodModel> generateCRUDMethods(EntityDefinition entity, @Context ProjectConfiguration config, boolean forImplementation) {
        List<GoRepositoryMethodModel> methods = new ArrayList<>();
        String entityName = entity.getEntityName();
        String entityNameLower = entityName.toLowerCase();
        String entityStructName = NamingUtils.toPascalCase(entityName);
        String modelPackagePrefix = deriveGoModelsPackage(config).substring(deriveGoModelsPackage(config).lastIndexOf('/') + 1) + ".";
        String entityModelType = "*" + modelPackagePrefix + entityStructName;
        String entityModelSliceType = "[]" + modelPackagePrefix + entityStructName;

        String idType = mapEntityIdType(entity);
        String idParamName = NamingUtils.toCamelCase(entityName) + "ID";
        GoParameterModel ctxParam = GoParameterModel.builder().name("ctx").type("context.Context").build();
        methods.add(GoRepositoryMethodModel.builder()
                .name("Create")
                .description("creates a new " + entityNameLower + " in the database.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(entityNameLower).type(entityModelType).build()))
                .returnTypes(List.of(GoParameterModel.builder().type(entityModelType).build(), GoParameterModel.builder().type("error").build()))
                .build());
        methods.add(GoRepositoryMethodModel.builder()
                .name("GetByID")
                .description("retrieves a " + entityNameLower + " by its ID.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(idParamName).type(idType).build()))
                .returnTypes(List.of(GoParameterModel.builder().type(entityModelType).build(), GoParameterModel.builder().type("error").build()))
                .build());
        methods.add(GoRepositoryMethodModel.builder()
                .name("Update")
                .description("updates an existing " + entityNameLower + " in the database.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(entityNameLower).type(entityModelType).build()))
                .returnTypes(List.of(GoParameterModel.builder().type(entityModelType).build(), GoParameterModel.builder().type("error").build()))
                .build());
        methods.add(GoRepositoryMethodModel.builder()
                .name("Delete")
                .description("deletes a " + entityNameLower + " by its ID.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(idParamName).type(idType).build()))
                .returnTypes(List.of(GoParameterModel.builder().type("error").build()))
                .build());
        methods.add(GoRepositoryMethodModel.builder()
                .name("List")
                .description("retrieves a list of " + entityNameLower + "s.")
                .parameters(List.of(ctxParam))
                .returnTypes(List.of(GoParameterModel.builder().type(entityModelSliceType).build(), GoParameterModel.builder().type("error").build()))
                .build());

        return methods;
    }
}