package org.maxpri.wagduck.generator.go.docker;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoDockerfileModel {
    private String goVersion;
    private String alpineVersion;
    private String appBinaryName;
    private String exposedPort;
}