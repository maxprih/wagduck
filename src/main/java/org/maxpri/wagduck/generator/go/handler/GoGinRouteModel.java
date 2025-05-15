package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoGinRouteModel {
    private String httpMethod;
    private String path;
    private String handlerFunctionName;
    private String description;
}