package org.maxpri.wagduck.generator.java.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaEntityModel {
    private String packageName; // e.g., org.maxpri.wagduck.domain.model
    private String className;   // e.g., User
    private String tableName;   // e.g., users or tbl_users
    private String description; // Class JavaDoc

    private boolean includeAuditing = true; // Flag to include @EntityListeners(AuditingEntityListener.class) and auditing fields
    private boolean useLombok = true;      // Flag from project options (assuming default true for now)

    private Set<String> imports; // All necessary imports
    private List<String> classAnnotations; // e.g., "@Entity", "@Table(name = "users")", Lombok, Auditing

    private List<JavaAttributeModel> attributes;
    private List<JavaRelationshipModel> relationships;

    private JavaAttributeModel createdAtAttribute;
    private JavaAttributeModel updatedAtAttribute;
}
