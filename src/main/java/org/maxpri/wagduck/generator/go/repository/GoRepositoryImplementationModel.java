package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoRepositoryImplementationModel {
    private String packageName; // e.g., repository
    private String structName; // e.g., gormUserRepository
    private String interfaceName; // e.g., UserRepository
    private String receiverName; // e.g., r (for method receivers like func (r *gormUserRepository)...)
    private String entityName; // e.g., User
    private String entityPackageName; // e.g., model
    private String entityStructName; // e.g., User
    private String entityIdType; // e.g., uint, string
    private String entityIdParameterName; // e.g., userID, id
    private String entityIdStructField; // The actual struct field name for ID, e.g., "ID" or "UUID"
    private Set<String> imports; // e.g., "context", "gorm.io/gorm", "github.com/your-module/internal/model"
    private List<GoRepositoryMethodModel> methods; // Methods from the interface to implement
    private String description; // e.g., "gormUserRepository implements UserRepository with GORM."
    private String modulePath;
}