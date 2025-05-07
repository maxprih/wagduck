package org.maxpri.wagduck.generator.java.build;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BuildFileModel {
    // Project Coordinates
    private String groupId;
    private String artifactId;
    private String version;
    private String projectName;
    private String projectDescription;
    private String basePackage; // Needed for main class name in some build plugins

    // Build Configuration
    private String javaVersion;
    private String springBootVersion;
    private Boolean useKotlin;

    // Dependencies (Grouped for clarity in template)
    private List<Dependency> springBootStarters;
    private List<Dependency> databaseDependencies;
    private List<Dependency> utilityDependencies; // Lombok, MapStruct etc.
    private List<Dependency> testDependencies;

    // Plugins (Map of plugin identifier to its configuration details/version)
    // This structure is flexible but might need refinement based on template needs
    private Map<String, String> plugins;

    // Static inner class for dependencies
    @Data
    @Builder
    public static class Dependency {
        private String groupId;
        private String artifactId;
        private String version; // Optional, managed by Spring Boot parent/platform often
        private String scope; // Optional (e.g., "test", "runtimeOnly", "annotationProcessor")
        private boolean isAnnotationProcessor; // Specific flag for Lombok/MapStruct in Gradle
    }
}