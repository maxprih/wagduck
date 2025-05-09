package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoGinRouteModel {
    private String httpMethod; // e.g., "POST", "GET", "PUT", "DELETE"
    private String path;       // e.g., "/", "/:id"
    private String handlerFunctionName; // e.g., "createUser", "getUserByID"
    private String description; // e.g., "Handles creation of a new user."
}