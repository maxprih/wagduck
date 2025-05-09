package org.maxpri.wagduck.generator.go.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoDbConfigFieldModel {
    private String fieldName; // e.g., Host, Port, User, Password, DBName, SSLMode
    private String fieldType; // e.g., "string", "int"
    private String envVarName; // e.g., "DB_HOST", "DB_PORT"
    private String defaultValue; // e.g., "localhost", "5432", "disable" (as a string)
    private boolean isSensitive; // True for fields like password, to potentially redact in logs if struct is printed
}