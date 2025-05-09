package org.maxpri.wagduck.generator.go.main;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoMainFileModel {
    private String packageName; // Should be "main"
    private String moduleName; // Full module path, e.g., "github.com/maxprih/mygoproject"
    private String configPackagePath; // e.g., "config" or "internal/config"
    private String modelsPackagePath; // e.g., "models" or "internal/models" (for AutoMigrate)
    private String repositoryPackagePath;
    private String servicePackagePath;
    private String handlerPackagePath;
    private Set<String> imports;
    private List<GoMainEntityWiringModel> entitiesToWire;
    private String serverPortEnvVar; // e.g., "SERVER_PORT"
    private String defaultServerPort; // e.g., "8080"
    private boolean autoMigrateEntities; // Flag to include GORM AutoMigrate
}