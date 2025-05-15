package org.maxpri.wagduck.generator.common.build;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BuildFileModel {
    private String groupId;
    private String artifactId;
    private String version;
    private String projectName;
    private String basePackage;

    private String javaVersion;
    private String springBootVersion;
    private boolean useKotlin;

    private String kotlinVersion;
    private String kotlinApiVersion;
    private String kotlinLanguageVersion;

    private String mainClassName;

    private List<Dependency> springBootStarters;
    private List<Dependency> databaseDependencies;
    private List<Dependency> utilityDependencies;
    private List<Dependency> testDependencies;

    private List<String> javaCompilerArgs;

    private Map<String, String> plugins;


    @Data
    @Builder
    public static class Dependency {
        private String groupId;
        private String artifactId;
        private String version;
        private String scope;
    }
}