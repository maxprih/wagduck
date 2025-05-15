package org.maxpri.wagduck.generator.go.main;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoMainEntityWiringModel {
    private String entityName;
    private String repositoryNewFunctionName;
    private String serviceNewFunctionName;
    private String handlerNewFunctionName;
    private String handlerSetupRoutesFunctionName;
}