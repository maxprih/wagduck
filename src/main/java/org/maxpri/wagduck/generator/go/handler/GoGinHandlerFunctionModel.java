package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoGinHandlerFunctionModel {
    private String name;
    private String description;

    private String serviceMethodName;
    private boolean expectsRequestBody;
    private String requestBodyType;
    private boolean hasPathParameter;
    private String pathParameterName;
    private String pathParameterType;
    private String successStatusCode;
    private String serviceParameterIdType;
}