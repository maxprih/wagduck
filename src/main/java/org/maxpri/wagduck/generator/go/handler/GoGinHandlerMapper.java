package org.maxpri.wagduck.generator.go.handler;

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
public interface GoGinHandlerMapper {

    @Mapping(target = "packageName", expression = "java(deriveGoHandlerPackage(config))")
    @Mapping(target = "handlerStructName", expression = "java(entity.getEntityName() + \"Handler\")")
    @Mapping(target = "receiverName", expression = "java(String.valueOf(entity.getEntityName().toLowerCase().charAt(0)) + \"h\")")
    @Mapping(target = "entityName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityNamePlural", expression = "java(org.maxpri.wagduck.util.NamingUtils.toSnakeCase(entity.getEntityName()))")
    @Mapping(target = "entityPackageName", expression = "java(deriveGoModelsPackagePath(config))") // Use full path
    @Mapping(target = "entityStructName", expression = "java(entity.getEntityName())")
    @Mapping(target = "serviceInterfaceName", expression = "java(entity.getEntityName() + \"Service\")")
    @Mapping(target = "serviceFieldName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Service\")")
    @Mapping(target = "servicePackageName", expression = "java(deriveGoServicePackagePath(config))") // Use full path
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectHandlerImports")
    @Mapping(target = "handlerFunctions", source = "entity", qualifiedByName = "generateHandlerFunctions")
    @Mapping(target = "routes", source = "entity", qualifiedByName = "generateRoutes")
    @Mapping(target = "baseRoutePath", expression = "java(\"/\" + org.maxpri.wagduck.util.NamingUtils.toSnakeCase(org.maxpri.wagduck.util.NamingUtils.toSnakeCase(entity.getEntityName())).replace('_', '-'))")
    @Mapping(target = "description", expression = "java(entity.getEntityName() + \"Handler handles HTTP requests for \" + entity.getEntityName().toLowerCase() + \" resources.\")")
    @Mapping(target = "moduleName", expression="java(config.getModuleName())") // Pass moduleName for template
    GoGinHandlerFileModel mapToHandlerFileModel(EntityDefinition entity, @Context ProjectConfiguration config);


    default String deriveGoHandlerPackage(@Context ProjectConfiguration config) {
        return "handler"; // Default
    }

    // Helper to get just the package name part if needed by template, from full path
    default String getBasePackageName(String fullPackagePath) {
        if (fullPackagePath == null) return "";
        if (fullPackagePath.contains("/")) {
            return fullPackagePath.substring(fullPackagePath.lastIndexOf('/') + 1);
        }
        return fullPackagePath;
    }

    default String deriveGoModelsPackagePath(@Context ProjectConfiguration config) {
        return config.getModuleName() + "/model"; // Default full path
    }

    default String deriveGoServicePackagePath(@Context ProjectConfiguration config) {
        return config.getModuleName() + "/service"; // Default full path
    }

    // Gets the actual Go type the service expects for an ID
    default String getActualServiceIdType(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .map(attr -> mapDefinitionTypeToServiceIdType(attr.getDataType()))
                .findFirst()
                .orElse("string"); // Fallback, though PK should exist
    }

    // Maps definition type to the Go type used by the service for an ID
    default String mapDefinitionTypeToServiceIdType(String definitionType) {
        if (definitionType == null) return "string";
        return switch (definitionType.toLowerCase()) {
            case "integer", "int" -> "int"; // Or int64 if service consistently uses it
            case "long", "bigint" -> "int64";
            case "uuid" -> "uuid.UUID"; // Service expects actual uuid.UUID
            // Add "uint" if your service uses uint and your DB stores it
            // case "uint_from_db" -> "uint";
            default -> "string"; // Fallback
        };
    }

    // Maps definition type to the type used for parsing path parameters (usually string or int for strconv)
    default String mapDefinitionTypeToPathParamParsingType(String definitionType) {
        if (definitionType == null) return "string";
        return switch (definitionType.toLowerCase()) {
            case "integer", "int", "long", "bigint" -> "int64"; // Path params are strings, parse to int64
            // case "uint_from_db" -> "uint64"; // Parse to uint64 then cast if needed
            case "uuid" -> "string"; // UUIDs are strings in path, then parsed to uuid.UUID
            default -> "string";
        };
    }


    @Named("collectHandlerImports")
    default Set<String> collectHandlerImports(EntityDefinition entity, @Context ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        // Gin, net/http are fixed in template.
        // Service and Model packages also fixed in template using full paths.

        String serviceIdType = getActualServiceIdType(entity);
        String pathParamParsingType = mapDefinitionTypeToPathParamParsingType(
                entity.getAttributes().stream()
                        .filter(AttributeDefinition::isPrimaryKey).findFirst()
                        .map(AttributeDefinition::getDataType).orElse(null)
        );

        if (pathParamParsingType.startsWith("int") || pathParamParsingType.startsWith("uint")) {
            imports.add("strconv");
        }
        if ("uuid.UUID".equals(serviceIdType)) {
            imports.add("github.com/google/uuid");
        }
        return imports;
    }

    @Named("generateHandlerFunctions")
    default List<GoGinHandlerFunctionModel> generateHandlerFunctions(EntityDefinition entity, @Context ProjectConfiguration config) {
        List<GoGinHandlerFunctionModel> functions = new ArrayList<>();
        String entityNamePascal = NamingUtils.toPascalCase(entity.getEntityName());

        String modelsPackageBasePath = getBasePackageName(deriveGoModelsPackagePath(config));
        String requestBodyType = modelsPackageBasePath + "." + entityNamePascal;

        String actualServiceIdType = getActualServiceIdType(entity);
        String pathParamParsingType = mapDefinitionTypeToPathParamParsingType(
                entity.getAttributes().stream()
                        .filter(AttributeDefinition::isPrimaryKey).findFirst()
                        .map(AttributeDefinition::getDataType).orElse(null)
        );

        // Create
        functions.add(GoGinHandlerFunctionModel.builder()
                .name("Create" + entityNamePascal)
                .description("handles creation of a " + entityNamePascal.toLowerCase() + ".")
                .serviceMethodName("Create" + entityNamePascal)
                .expectsRequestBody(true)
                .requestBodyType(requestBodyType)
                .successStatusCode("http.StatusCreated")
                .build());

        // Get By ID
        functions.add(GoGinHandlerFunctionModel.builder()
                .name("Get" + entityNamePascal + "ByID")
                .description("handles fetching a " + entityNamePascal.toLowerCase() + " by ID.")
                .serviceMethodName("Get" + entityNamePascal + "ByID")
                .hasPathParameter(true)
                .pathParameterName("id")
                .pathParameterType(pathParamParsingType) // Type for initial parsing
                .serviceParameterIdType(actualServiceIdType) // Actual type service needs
                .successStatusCode("http.StatusOK")
                .build());

        // Update
        functions.add(GoGinHandlerFunctionModel.builder()
                .name("Update" + entityNamePascal)
                .description("handles updating a " + entityNamePascal.toLowerCase() + ".")
                .serviceMethodName("Update" + entityNamePascal)
                .expectsRequestBody(true)
                .requestBodyType(requestBodyType)
                .hasPathParameter(true)
                .pathParameterName("id")
                .pathParameterType(pathParamParsingType)
                .serviceParameterIdType(actualServiceIdType)
                .successStatusCode("http.StatusOK")
                .build());

        // Delete
        functions.add(GoGinHandlerFunctionModel.builder()
                .name("Delete" + entityNamePascal)
                .description("handles deleting a " + entityNamePascal.toLowerCase() + " by ID.")
                .serviceMethodName("Delete" + entityNamePascal)
                .hasPathParameter(true)
                .pathParameterName("id")
                .pathParameterType(pathParamParsingType)
                .serviceParameterIdType(actualServiceIdType)
                .successStatusCode("http.StatusNoContent")
                .build());

        // List
        functions.add(GoGinHandlerFunctionModel.builder()
                .name("List" + entityNamePascal + "s")
                .description("handles listing all " + entityNamePascal.toLowerCase() + "s.")
                .serviceMethodName("List" + entityNamePascal + "s")
                .successStatusCode("http.StatusOK")
                .build());

        return functions;
    }

    @Named("generateRoutes")
    default List<GoGinRouteModel> generateRoutes(EntityDefinition entity, @Context ProjectConfiguration config) {
        List<GoGinRouteModel> routes = new ArrayList<>();
        String entityNamePascal = NamingUtils.toPascalCase(entity.getEntityName());

        routes.add(GoGinRouteModel.builder().httpMethod("POST").path("").handlerFunctionName("Create" + entityNamePascal).build());
        routes.add(GoGinRouteModel.builder().httpMethod("GET").path("/:id").handlerFunctionName("Get" + entityNamePascal + "ByID").build());
        routes.add(GoGinRouteModel.builder().httpMethod("PUT").path("/:id").handlerFunctionName("Update" + entityNamePascal).build());
        routes.add(GoGinRouteModel.builder().httpMethod("DELETE").path("/:id").handlerFunctionName("Delete" + entityNamePascal).build());
        routes.add(GoGinRouteModel.builder().httpMethod("GET").path("").handlerFunctionName("List" + entityNamePascal + "s").build());
        return routes;
    }
}