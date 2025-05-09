package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoRepositoryInterfaceModel {
    private String packageName; // e.g., repository
    private String interfaceName; // e.g., UserRepository
    private String entityName; // e.g., User (PascalCase)
    private String entityPackageName; // e.g., model (where User struct is defined)
    private String entityStructName; // e.g., User
    private String entityIdType; // e.g., uint, string (for GetByID, Delete)
    private String entityIdParameterName; // e.g., userID, id
    private Set<String> imports; // e.g., "context", "github.com/your-module/internal/model"
    private List<GoRepositoryMethodModel> methods;
    private String description; // e.g., "UserRepository defines the interface for user data operations."
    private String modulePath;
}