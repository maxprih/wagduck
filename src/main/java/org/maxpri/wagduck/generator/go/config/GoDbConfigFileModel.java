package org.maxpri.wagduck.generator.go.config;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoDbConfigFileModel {
    private String packageName;
    private String configStructName;
    private String loadFunctionName;
    private String dsnFunctionName;
    private String initDbFunctionName;
    private Set<String> imports;
    private List<GoDbConfigFieldModel> fields;
}