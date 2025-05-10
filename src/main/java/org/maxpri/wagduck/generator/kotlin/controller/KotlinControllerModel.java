package org.maxpri.wagduck.generator.kotlin.controller;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class KotlinControllerModel {
    private String packageName;              // e.g., org.maxpri.wagduck.controller
    private Set<String> imports;             // All necessary imports

    private String controllerClassName;      // e.g., UserController
    private String baseRequestPath;          // e.g., /api/v1/users

    private String serviceClassName;         // e.g., UserService
    private String serviceFieldName;         // e.g., userService
    private String serviceClassImport;       // e.g., org.maxpri.wagduck.service.UserService

    private String requestDtoClassName;      // e.g., UserRequestDto
    private String requestDtoClassImport;    // e.g., org.maxpri.wagduck.dto.UserRequestDto
    private String responseDtoClassName;     // e.g., UserResponseDto
    private String responseDtoClassImport;   // e.g., org.maxpri.wagduck.dto.UserResponseDto

    private String primaryKeyType;           // Kotlin type of the PK, e.g., Long, java.util.UUID
    private String primaryKeyName;           // Name of the PK path variable, e.g., "id" (matches service param name)

    private String entityClassNameForLogging; // e.g., User (for log messages)
    private String entityNotFoundExceptionImport; // e.g., org.maxpri.wagduck.exception.EntityNotFoundException
    private String entityNotFoundExceptionName;   // e.g., EntityNotFoundException
}