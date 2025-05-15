package org.maxpri.wagduck.generator.go.docker;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;

@Mapper(componentModel = "spring")
public interface GoDockerfileMapper {

    @Mapping(target = "goVersion", expression = "java(getProjectGoVersion(config, \"1.21\"))")
    @Mapping(target = "alpineVersion", constant = "3.19")
    @Mapping(target = "appBinaryName", expression = "java(getProjectAppName(config, \"app\"))")
    @Mapping(target = "exposedPort", constant = "8080")
    GoDockerfileModel mapToDockerfileModel(ProjectConfiguration config);

    default String getProjectGoVersion(ProjectConfiguration config, String defaultValue) {
        return config.getLanguageVersion() != null ? config.getLanguageVersion() : defaultValue;
    }

    default String getProjectAppName(ProjectConfiguration config, String defaultValue) {
        if (config != null && config.getModuleName() != null && !config.getModuleName().isEmpty()) {
            String moduleName = config.getModuleName();
            if (moduleName.contains("/")) {
                return moduleName.substring(moduleName.lastIndexOf('/') + 1);
            }
            return moduleName;
        }
        return defaultValue;
    }
}