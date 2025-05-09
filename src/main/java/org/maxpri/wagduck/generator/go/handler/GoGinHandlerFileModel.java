package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoGinHandlerFileModel {
    private String packageName; // e.g., handler or web
    private String handlerStructName; // e.g., UserHandler
    private String receiverName; // e.g., h (for func (h *UserHandler)...)
    private String entityName; // e.g., User
    private String entityNamePlural; // e.g., Users
    private String entityPackageName; // e.g., models
    private String entityStructName; // e.g., User
    // private String dtoPackageName; // if using DTOs
    private String serviceInterfaceName; // e.g., UserService
    private String serviceFieldName; // e.g., userService
    private String servicePackageName; // e.g., service
    private Set<String> imports;
    private List<GoGinHandlerFunctionModel> handlerFunctions;
    private List<GoGinRouteModel> routes; // Routes this handler will register
    private String baseRoutePath; // e.g., "/users", "/products"
    private String description;
    private String moduleName;
}