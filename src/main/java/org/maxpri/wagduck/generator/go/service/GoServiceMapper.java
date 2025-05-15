package org.maxpri.wagduck.generator.go.service;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.go.repository.GoParameterModel;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface GoServiceMapper {

    @Mapping(target = "packageName", expression = "java(deriveGoServicePackage(config))")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Service\")")
    @Mapping(target = "entityName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackageName", expression = "java(deriveGoModelsPackagePath(config))")
    @Mapping(target = "entityStructName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityIdType", source = "entity", qualifiedByName = "mapEntityIdTypeFromEntity")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectServiceInterfaceImports")
    @Mapping(target = "methods", source = "entity", qualifiedByName = "generateCRUDMethodsForService")
    @Mapping(target = "description", expression = "java(entity.getEntityName() + \"Service defines the interface for \" + entity.getEntityName().toLowerCase() + \" business operations.\")")
    GoServiceInterfaceModel mapToInterfaceModel(EntityDefinition entity, @Context ProjectConfiguration config);

    @Mapping(target = "packageName", expression = "java(deriveGoServicePackage(config))")
    @Mapping(target = "structName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"ServiceImpl\")")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Service\")")
    @Mapping(target = "receiverName", expression = "java(String.valueOf(entity.getEntityName().toLowerCase().charAt(0)) + \"s\")")
    @Mapping(target = "entityName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackageName", expression = "java(deriveGoModelsPackagePath(config))")
    @Mapping(target = "entityStructName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityIdType", source = "entity", qualifiedByName = "mapEntityIdTypeFromEntity")
    @Mapping(target = "repositoryInterfaceName", expression = "java(entity.getEntityName() + \"Repository\")")
    @Mapping(target = "repositoryFieldName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Repo\")")
    @Mapping(target = "repositoryPackageName", expression = "java(deriveGoRepositoryPackagePath(config))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectServiceImplementationImports")
    @Mapping(target = "methods", source = "entity", qualifiedByName = "generateCRUDMethodsForService")
    @Mapping(target = "description", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"ServiceImpl implements \" + entity.getEntityName() + \"Service.\")")
    GoServiceImplementationModel mapToImplementationModel(EntityDefinition entity, @Context ProjectConfiguration config);

    default String deriveGoServicePackage(@Context ProjectConfiguration config) {
        return "service";
    }

    default String deriveGoModelsPackagePath(@Context ProjectConfiguration config) {
        return config.getModuleName() + "/model";
    }

    default String deriveGoRepositoryPackagePath(@Context ProjectConfiguration config) {
        return config.getModuleName() + "/repository";
    }


    @Named("mapEntityIdTypeFromEntity")
    default String mapEntityIdTypeFromEntity(EntityDefinition entity) {
        return entity.getAttributes().stream()
                .filter(AttributeDefinition::isPrimaryKey)
                .map(attr -> mapToGoDataTypeForService(attr.getDataType()))
                .findFirst()
                .orElse("uint");
    }

    default String mapToGoDataTypeForService(String definitionType) {
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

    @Named("collectServiceInterfaceImports")
    default Set<String> collectServiceInterfaceImports(EntityDefinition entity, @Context ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        imports.add("context");
        imports.add(deriveGoModelsPackagePath(config));

        String idType = mapEntityIdTypeFromEntity(entity);
        if ("uuid.UUID".equals(idType)) imports.add("github.com/google/uuid");
        if (idType.contains("time.Time")) imports.add("time");
        if (idType.contains("decimal.Decimal")) imports.add("github.com/shopspring/decimal");
        return imports;
    }

    @Named("collectServiceImplementationImports")
    default Set<String> collectServiceImplementationImports(EntityDefinition entity, @Context ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        imports.add("context");
        imports.add(deriveGoModelsPackagePath(config));
        imports.add(deriveGoRepositoryPackagePath(config));


        String idType = mapEntityIdTypeFromEntity(entity);
        if ("uuid.UUID".equals(idType)) imports.add("github.com/google/uuid");
        boolean needsTimeImport = false;
        boolean needsDecimalImport = false;

        if (idType.contains("time.Time")) needsTimeImport = true;
        if (idType.contains("decimal.Decimal")) needsDecimalImport = true;

        for (AttributeDefinition attr : entity.getAttributes()) {
            String attrGoType = mapToGoDataTypeForService(attr.getDataType());
            if (attrGoType.equals("time.Time")) needsTimeImport = true;
            if (attrGoType.equals("decimal.Decimal")) needsDecimalImport = true;
        }

        if (needsTimeImport) imports.add("time");
        if (needsDecimalImport) imports.add("github.com/shopspring/decimal");

        return imports;
    }

    @Named("generateCRUDMethodsForService")
    default List<GoServiceMethodModel> generateCRUDMethodsForService(EntityDefinition entity, @Context ProjectConfiguration config) {
        List<GoServiceMethodModel> methods = new ArrayList<>();
        String entityNamePascal = NamingUtils.toPascalCase(entity.getEntityName());
        String entityNameCamel = NamingUtils.toCamelCase(entity.getEntityName());
        String modelsPackageNameOnly = deriveGoModelsPackagePath(config);
        if (modelsPackageNameOnly.contains("/")) {
            modelsPackageNameOnly = modelsPackageNameOnly.substring(modelsPackageNameOnly.lastIndexOf('/') + 1);
        }

        String modelType = "*" + modelsPackageNameOnly + "." + entityNamePascal;
        String modelSliceType = "[]" + modelsPackageNameOnly + "." + entityNamePascal;
        String createRequestType = modelType;
        String updateRequestDetailsType = modelType;
        String responseType = modelType;
        String responseSliceType = modelSliceType;

        String idType = mapEntityIdTypeFromEntity(entity);
        String idParamName = entityNameCamel + "ID";

        GoParameterModel ctxParam = GoParameterModel.builder().name("ctx").type("context.Context").build();
        methods.add(GoServiceMethodModel.builder()
                .name("Create" + entityNamePascal)
                .description("handles creating a new " + entity.getEntityName().toLowerCase() + ".")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(entityNameCamel).type(createRequestType).build()))
                .returnTypes(List.of(GoParameterModel.builder().name("created" + entityNamePascal).type(responseType).build(), GoParameterModel.builder().name("err").type("error").build()))
                .correspondingRepositoryMethodName("Create")
                .build());
        methods.add(GoServiceMethodModel.builder()
                .name("Get" + entityNamePascal + "ByID")
                .description("handles retrieving a " + entity.getEntityName().toLowerCase() + " by its ID.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(idParamName).type(idType).build()))
                .returnTypes(List.of(GoParameterModel.builder().name(entityNameCamel).type(responseType).build(), GoParameterModel.builder().name("err").type("error").build()))
                .correspondingRepositoryMethodName("GetByID")
                .build());
        List<UpdatableField> updatableFieldsForEntity = entity.getAttributes().stream()
                .filter(attr -> {
                    String attrNameLower = attr.getAttributeName().toLowerCase();
                    String colNameLower = attr.getColumnName() != null ? attr.getColumnName().toLowerCase() : "";

                    if (attr.isPrimaryKey() ||
                            attrNameLower.equals("createdat") || attrNameLower.equals("created_at") || colNameLower.equals("created_at") ||
                            attrNameLower.equals("updatedat") || attrNameLower.equals("updated_at") || colNameLower.equals("updated_at") ||
                            attrNameLower.equals("deletedat") || attrNameLower.equals("deleted_at") || colNameLower.equals("deleted_at") ||
                            attrNameLower.equals("id") && !attr.isPrimaryKey()
                    ) {
                        return false;
                    }
                    return true;
                })
                .map(attr -> UpdatableField.builder()
                        .goFieldName(NamingUtils.toPascalCase(attr.getAttributeName()))
                        .goFieldType(mapToGoDataTypeForService(attr.getDataType()))
                        .build())
                .collect(Collectors.toList());

        methods.add(GoServiceMethodModel.builder()
                .name("Update" + entityNamePascal)
                .description("handles updating an existing " + entity.getEntityName().toLowerCase() + ".")
                .parameters(List.of(
                        ctxParam,
                        GoParameterModel.builder().name(idParamName).type(idType).build(),
                        GoParameterModel.builder().name(entityNameCamel + "Details").type(updateRequestDetailsType).build()
                ))
                .returnTypes(List.of(
                        GoParameterModel.builder().name("updated" + entityNamePascal).type(responseType).build(),
                        GoParameterModel.builder().name("err").type("error").build()
                ))
                .correspondingRepositoryMethodName("Update")
                .updatableFields(updatableFieldsForEntity)
                .build());
        methods.add(GoServiceMethodModel.builder()
                .name("Delete" + entityNamePascal)
                .description("handles deleting a " + entity.getEntityName().toLowerCase() + " by its ID.")
                .parameters(List.of(ctxParam, GoParameterModel.builder().name(idParamName).type(idType).build()))
                .returnTypes(List.of(GoParameterModel.builder().name("err").type("error").build()))
                .correspondingRepositoryMethodName("Delete")
                .build());
        methods.add(GoServiceMethodModel.builder()
                .name("List" + entityNamePascal + "s")
                .description("handles retrieving a list of " + entity.getEntityName().toLowerCase() + "s.")
                .parameters(List.of(ctxParam))
                .returnTypes(List.of(GoParameterModel.builder().name(entityNameCamel + "List").type(responseSliceType).build(), GoParameterModel.builder().name("err").type("error").build()))
                .correspondingRepositoryMethodName("List")
                .build());
        return methods;
    }
}