package org.maxpri.wagduck.generator.go.config;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoDbConfigFileModel {
    private String packageName; // e.g., config
    private String configStructName; // e.g., DatabaseConfig
    private String loadFunctionName; // e.g., LoadDatabaseConfig
    private String dsnFunctionName; // e.g., DSN
    private String initDbFunctionName; // e.g., InitDatabaseConnection
    private Set<String> imports; // e.g., "fmt", "os", "strconv", "log", "gorm.io/gorm", "gorm.io/driver/postgres"
    private List<GoDbConfigFieldModel> fields;
}