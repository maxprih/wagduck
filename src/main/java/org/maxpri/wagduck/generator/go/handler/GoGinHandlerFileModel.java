package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoGinHandlerFileModel {
    private String packageName;
    private String handlerStructName;
    private String receiverName;
    private String entityName;
    private String entityNamePlural;
    private String entityPackageName;
    private String entityStructName;
    private String serviceInterfaceName;
    private String serviceFieldName;
    private String servicePackageName;
    private Set<String> imports;
    private List<GoGinHandlerFunctionModel> handlerFunctions;
    private List<GoGinRouteModel> routes;
    private String baseRoutePath;
    private String description;
    private String moduleName;
}