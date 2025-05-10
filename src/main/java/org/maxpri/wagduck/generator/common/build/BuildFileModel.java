package org.maxpri.wagduck.generator.common.build;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map; // Keep for plugins if general config is needed

@Data
@Builder
public class BuildFileModel {
    // Project Coordinates
    private String groupId;
    private String artifactId;
    private String version;
    private String projectName;
    // private String projectDescription; // Was in your original, uncomment if needed
    private String basePackage;

    // Build Configuration
    private String javaVersion;
    private String springBootVersion;
    private boolean useKotlin; // Changed to primitive boolean

    // Kotlin Specific (if useKotlin is true)
    private String kotlinVersion;
    private String kotlinApiVersion;
    private String kotlinLanguageVersion;

    // Main Class for Spring Boot
    private String mainClassName;

    // Dependencies
    private List<Dependency> springBootStarters;
    private List<Dependency> databaseDependencies;
    private List<Dependency> utilityDependencies;
    private List<Dependency> testDependencies;

    // Optional: For passing generic args to JavaCompile if still needed alongside Kapt
    private List<String> javaCompilerArgs;

    // Plugins map - might be less used for Gradle template which hardcodes task configs
    // but can be kept for flexibility or if some dynamic plugin config is needed.
    private Map<String, String> plugins;


    @Data
    @Builder
    public static class Dependency {
        private String groupId;
        private String artifactId;
        private String version; // Optional, can be managed by Spring Boot BOM
        private String scope;   // e.g., "implementation", "testImplementation", "runtimeOnly", "compileOnly", "annotationProcessor", "kapt"
        // isAnnotationProcessor flag is redundant if scope clearly indicates it.
    }
}