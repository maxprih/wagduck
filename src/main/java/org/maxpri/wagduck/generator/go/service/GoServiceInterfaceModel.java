package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoServiceInterfaceModel {
    private String packageName; // e.g., service
    private String interfaceName; // e.g., UserService
    private String entityName; // e.g., User (PascalCase)
    private String entityPackageName; // e.g., models (where User struct is defined)
    private String entityStructName; // e.g., User
    private String entityIdType; // e.g., uint, string (for GetByID, Delete)
    // DTO package if we were using DTOs: private String dtoPackageName;
    private Set<String> imports; // e.g., "context", "github.com/your-module/internal/models"
    private List<GoServiceMethodModel> methods;
    private String description; // e.g., "UserService defines the interface for user business logic."
    private String modulePath;
}