package org.maxpri.wagduck.generator.go.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoDbConfigFieldModel {
    private String fieldName;
    private String fieldType;
    private String envVarName;
    private String defaultValue;
    private boolean isSensitive;
}