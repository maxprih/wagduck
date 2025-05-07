package org.maxpri.wagduck.dto.request.project;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;

import java.util.Set;

@Data
@Builder
public class ProjectConfigurationCreateRequest {

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100, message = "Project name must be less than 100 characters")
    private String projectName;

    @NotNull(message = "Target language must be specified")
    private TargetLanguage language;

    @NotNull(message = "Target framework must be specified")
    private TargetFramework framework;

    @Size(max = 20, message = "Language version must be less than 20 characters")
    private String languageVersion;

    @Size(max = 20, message = "Framework version must be less than 20 characters")
    private String frameworkVersion;

    // Java/Kotlin-specific
    private BuildTool buildTool;

    @Pattern(regexp = "^([a-zA-Z_]{1}[a-zA-Z0-9_]*(\\.[a-zA-Z_]{1}[a-zA-Z0-9_]*)*)?$", message = "Invalid Java/Kotlin package name format")
    @Size(max = 255)
    private String basePackage;

    @Size(max = 255)
    private String moduleName;

    @NotNull(message = "Database type must be specified")
    private DatabaseType databaseType;

    private Set<String> enabledOptions;
}
