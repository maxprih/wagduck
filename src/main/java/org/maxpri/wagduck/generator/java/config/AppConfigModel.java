package org.maxpri.wagduck.generator.java.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppConfigModel {
    // Server
    private String serverPort;

    // Database
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriverClassName; // Optional, Spring Boot often infers
    private String jpaDdlAuto; // e.g., "update", "validate", "none"
    private String jpaDatabasePlatform;
    private boolean jpaShowSql;

    // Application specific
    private String applicationName; // From artifactId
    private String basePackage;

    // Add other properties as needed (logging levels, security config, etc.)
}