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


    // --- Helper Methods ---
    default String deriveGoConfigPackagePath(ProjectConfiguration config) {
        // return config.getGoConfigPackagePath(); // Ideal
        return "/config"; // Default
    }

    default String deriveGoModelsPackagePath(ProjectConfiguration config) {
        // return config.getGoModelsPackagePath(); // Ideal
        return "/model"; // Default
    }

    default String deriveGoRepositoryPackagePath(ProjectConfiguration config) {
        // return config.getGoRepositoryPackagePath(); // Ideal
        return "/repository"; // Default
    }

    default String deriveGoServicePackagePath(ProjectConfiguration config) {
        // return config.getGoServicePackagePath(); // Ideal
        return "/service"; // Default
    }

    default String deriveGoHandlerPackagePath(ProjectConfiguration config) {
        // return config.getGoHandlerPackagePath(); // Ideal
        return "/handler"; // Default
    }

    default boolean checkAutoMigrateOption(ProjectConfiguration config) {
        // Example: return config.getGoOptions().getBoolean("AUTO_MIGRATE_ENTITIES", true);
        return true; // Default to true for now
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
                        .entityNamePlural(NamingUtils.toPascalCase(entity.getEntityName()) + "s")
                        // Assuming package names are just the last part of the path for aliases in template
                        .repositoryPackageAlias(deriveGoRepositoryPackagePath(config).substring(deriveGoRepositoryPackagePath(config).lastIndexOf('/') + 1))
                        .repositoryStructName("gorm" + NamingUtils.toPascalCase(entity.getEntityName()) + "Repository")
                        .repositoryNewFunctionName("NewGorm" + NamingUtils.toPascalCase(entity.getEntityName()) + "Repository") // Matches GormRepository template
                        .servicePackageAlias(deriveGoServicePackagePath(config).substring(deriveGoServicePackagePath(config).lastIndexOf('/') + 1))
                        .serviceStructName(NamingUtils.toCamelCase(entity.getEntityName()) + "ServiceImpl")
                        .serviceNewFunctionName("New" + NamingUtils.toPascalCase(entity.getEntityName()) + "Service") // Matches Service template
                        .handlerPackageAlias(deriveGoHandlerPackagePath(config).substring(deriveGoHandlerPackagePath(config).lastIndexOf('/') + 1))
                        .handlerStructName(NamingUtils.toPascalCase(entity.getEntityName()) + "Handler")
                        .handlerNewFunctionName("New" + NamingUtils.toPascalCase(entity.getEntityName()) + "Handler") // Matches Handler template
                        .handlerSetupRoutesFunctionName("Setup" + NamingUtils.toPascalCase(entity.getEntityName()) + "Routes") // Matches Handler template
                        .build())
                .collect(Collectors.toList());
    }
}