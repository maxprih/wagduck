package org.maxpri.wagduck.dto.response.project;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ProjectConfigurationResponse {
    private UUID id;
    private String projectName;
    private TargetLanguage language;
    private TargetFramework framework;
    private String languageVersion;
    private String frameworkVersion;
    private BuildTool buildTool;
    private String basePackage;
    private String moduleName;
    private DatabaseType databaseType;
    private Set<String> enabledOptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}