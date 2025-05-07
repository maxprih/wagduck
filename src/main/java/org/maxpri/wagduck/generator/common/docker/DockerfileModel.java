package org.maxpri.wagduck.generator.common.docker;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.BuildTool;

@Data
@Builder
public class DockerfileModel {
    private String javaVersion;
    private BuildTool buildTool;
    private String jarNamePattern;
    private String appPort;
    private String mavenVersion;
    private String gradleVersion;
}