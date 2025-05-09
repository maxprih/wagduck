package org.maxpri.wagduck.generator.go.handler;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoGinHandlerFunctionModel {
    private String name; // e.g., CreateUser, GetUserByID
    private String description;
    // Parameters for the Gin handler function itself (usually just "c *gin.Context")
    // The actual data parameters are extracted from 'c' inside the function body.

    // Information about the service call it makes:
    private String serviceMethodName; // e.g., "CreateUser", "GetUserByID"
    private boolean expectsRequestBody; // True if it needs to bind a JSON body
    private String requestBodyType; // e.g., "models.User" or "dto.UserCreateRequest" (if using DTOs)
    private boolean hasPathParameter; // True if it uses something like /:id
    private String pathParameterName; // e.g., "id"
    private String pathParameterType; // Go type for the path parameter (e.g. "string", "int") for parsing
    private String successStatusCode; // e.g., "http.StatusCreated", "http.StatusOK"
    private String serviceParameterIdType;  // The actual type the service method expects for the ID (e.g., "int64", "uint", "uuid.UUID")
    // List of expected query parameters (more advanced)
}