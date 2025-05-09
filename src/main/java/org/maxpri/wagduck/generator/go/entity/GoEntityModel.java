package org.maxpri.wagduck.generator.go.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoEntityModel {
    private String packageName; // e.g., model
    private String structName;   // e.g., User (PascalCase)
    private String tableName;   // e.g., "users" or "" if default GORM naming is fine
    private String description; // Struct comment

    private boolean embedGormModel; // Flag to embed gorm.Model (provides ID, CreatedAt, UpdatedAt, DeletedAt)
    // If embedGormModel is false, the following can be used:
    private GoFieldModel idField; // Custom ID field
    private GoFieldModel createdAtField; // Custom CreatedAt field
    private GoFieldModel updatedAtField; // Custom UpdatedAt field
    // private GoFieldModel deletedAtField; // Custom DeletedAt field (for soft delete) -> gorm.DeletedAt

    private Set<String> imports; // All necessary Go imports (e.g., "time", "gorm.io/gorm")
    private List<GoFieldModel> attributes; // Regular fields (non-relational)
    private List<GoFieldModel> relationships; // Relational fields
}