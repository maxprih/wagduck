package org.maxpri.wagduck.generator.go.docker;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoDockerfileModel {
    private String goVersion; // e.g., "1.21"
    private String alpineVersion; // e.g., "3.19" (for the final stage)
    private String appBinaryName; // Name of the compiled executable, e.g., "myservice"
    private String exposedPort; // Port the application listens on inside the container, e.g., "8080"
}