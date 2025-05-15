package org.maxpri.wagduck.generator.go.main;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoMainFileModel {
    private String packageName;
    private String moduleName;
    private String configPackagePath;
    private String modelsPackagePath;
    private String repositoryPackagePath;
    private String servicePackagePath;
    private String handlerPackagePath;
    private Set<String> imports;
    private List<GoMainEntityWiringModel> entitiesToWire;
    private String serverPortEnvVar;
    private String defaultServerPort;
    private boolean autoMigrateEntities;
}