package org.maxpri.wagduck.generator.common.docker;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.BuildTool;

@Mapper(componentModel = "spring")
public interface DockerfileMapper {

    String DEFAULT_GRADLE_VERSION = "8.7";
    String DEFAULT_MAVEN_VERSION = "3.9.6";

    @Mapping(target = "javaVersion", source = "languageVersion")
    @Mapping(target = "buildTool", source = "buildTool")
    @Mapping(target = "jarNamePattern", expression = "java(deriveJarNamePattern(config))")
    @Mapping(target = "appPort", constant = "8080")
    @Mapping(target = "mavenVersion", constant = DEFAULT_MAVEN_VERSION)
    @Mapping(target = "gradleVersion", constant = DEFAULT_GRADLE_VERSION)
    DockerfileModel toDockerfileModel(ProjectConfiguration config);

    default String deriveJarNamePattern(ProjectConfiguration config) {
        String baseName = config.getModuleName() != null ? config.getModuleName() : "app";
        if (config.getBuildTool() == BuildTool.GRADLE) {
            return "*.jar";
        } else {
            return baseName + "-*.jar";
        }
    }
}