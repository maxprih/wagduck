package org.maxpri.wagduck.generator.common.compose;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import java.util.Map;

@Data
@Builder
public class DockerComposeModel {
    private String appServiceName;      // e.g., "my-app" (from moduleName)
    private String appPort;                // e.g., 8080
    private DatabaseType databaseType;  // POSTGRESQL, MYSQL, H2, NONE
    private String dbServiceName;       // e.g., "db"
    private String dbImage;             // e.g., "postgres:15", "mysql:8"
    private String dbVolumeName;        // e.g., "db-data"
    private String dbPort;                 // e.g., 5432, 3306
    private String dbName;              // Database name (e.g., from moduleName)
    private String dbUser;              // Default user
    private String dbPassword;          // Default password
    private Map<String, String> dbEnvVars; // Environment variables for the DB container
    private String appDbUrlEnvVar;      // Full JDBC URL for the app container using service name
}
