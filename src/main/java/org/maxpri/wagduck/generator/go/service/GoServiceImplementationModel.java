package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoServiceImplementationModel {
    private String packageName; // e.g., service
    private String structName; // e.g., userServiceImpl
    private String interfaceName; // e.g., UserService
    private String receiverName; // e.g., s (for method receivers like func (s *userServiceImpl)...)
    private String entityName; // e.g., User
    private String entityPackageName; // e.g., models
    private String entityStructName; // e.g., User
    private String entityIdType; // e.g., uint, string
    private String repositoryInterfaceName; // e.g., UserRepository
    private String repositoryFieldName; // e.g., userRepo
    private String repositoryPackageName; // e.g., repository
    // DTO package if we were using DTOs: private String dtoPackageName;
    private Set<String> imports; // e.g., "context", "github.com/your-module/internal/models", "github.com/your-module/internal/repository"
    private List<GoServiceMethodModel> methods; // Methods from the interface to implement
    private String description; // e.g., "userServiceImpl implements UserService."
    private String modulePath;
}