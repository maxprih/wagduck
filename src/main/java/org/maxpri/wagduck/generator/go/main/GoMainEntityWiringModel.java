package org.maxpri.wagduck.generator.go.main;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoMainEntityWiringModel {
    private String entityName; // e.g., User
    private String entityNamePlural; // e.g., Users
    private String repositoryPackageAlias; // e.g., "repo" or "" if in same package
    private String repositoryStructName;   // e.g., gormUserRepository
    private String repositoryNewFunctionName; // e.g., NewGormUserRepository or NewUserRepository
    private String servicePackageAlias;    // e.g., "svc"
    private String serviceStructName;      // e.g., userServiceImpl
    private String serviceNewFunctionName;   // e.g., NewUserService
    private String handlerPackageAlias;    // e.g., "hdlr"
    private String handlerStructName;      // e.g., UserHandler
    private String handlerNewFunctionName;   // e.g., NewUserHandler
    private String handlerSetupRoutesFunctionName; // e.g., SetupUserRoutes
}