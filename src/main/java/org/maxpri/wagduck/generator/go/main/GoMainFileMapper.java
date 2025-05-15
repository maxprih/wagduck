package org.maxpri.wagduck.generator.go.main;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface GoMainFileMapper {

    @Mapping(target = "packageName", expression = "java(\"main\")")
    @Mapping(target = "moduleName", expression = "java(config.getModuleName())")
    @Mapping(target = "configPackagePath", expression = "java(deriveGoConfigPackagePath(config))")
    @Mapping(target = "modelsPackagePath", expression = "java(deriveGoModelsPackagePath(config))")
    @Mapping(target = "repositoryPackagePath", expression = "java(deriveGoRepositoryPackagePath(config))")
    @Mapping(target = "servicePackagePath", expression = "java(deriveGoServicePackagePath(config))")
    @Mapping(target = "handlerPackagePath", expression = "java(deriveGoHandlerPackagePath(config))")
    @Mapping(target = "imports", source = "config", qualifiedByName = "collectMainImports")
    @Mapping(target = "entitiesToWire", source = "config", qualifiedByName = "mapEntitiesToWiringModels")
    @Mapping(target = "serverPortEnvVar", expression = "java(\"SERVER_PORT\")")
    @Mapping(target = "defaultServerPort", expression = "java(\"8080\")")
    @Mapping(target = "autoMigrateEntities", expression = "java(checkAutoMigrateOption(config))")
    GoMainFileModel mapToMainFileModel(ProjectConfiguration config);

    default String deriveGoConfigPackagePath(ProjectConfiguration config) {
        return "/config";
    }

    default String deriveGoModelsPackagePath(ProjectConfiguration config) {
        return "/model";
    }

    default String deriveGoRepositoryPackagePath(ProjectConfiguration config) {
        return "/repository";
    }

    default String deriveGoServicePackagePath(ProjectConfiguration config) {
        return "/service";
    }

    default String deriveGoHandlerPackagePath(ProjectConfiguration config) {
        return "/handler";
    }

    default boolean checkAutoMigrateOption(ProjectConfiguration config) {
        return true;
    }

    @Named("collectMainImports")
    default Set<String> collectMainImports(ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        return imports;
    }

    @Named("mapEntitiesToWiringModels")
    default List<GoMainEntityWiringModel> mapEntitiesToWiringModels(ProjectConfiguration config) {
        if (config.getEntities() == null) {
            return new ArrayList<>();
        }
        return config.getEntities().stream()
                .map(entity -> GoMainEntityWiringModel.builder()
                        .entityName(NamingUtils.toPascalCase(entity.getEntityName()))
                        .repositoryNewFunctionName("NewGorm" + NamingUtils.toPascalCase(entity.getEntityName()) + "Repository")
                        .serviceNewFunctionName("New" + NamingUtils.toPascalCase(entity.getEntityName()) + "Service")
                        .handlerNewFunctionName("New" + NamingUtils.toPascalCase(entity.getEntityName()) + "Handler")
                        .handlerSetupRoutesFunctionName("Setup" + NamingUtils.toPascalCase(entity.getEntityName()) + "Routes")
                        .build())
                .collect(Collectors.toList());
    }
}
