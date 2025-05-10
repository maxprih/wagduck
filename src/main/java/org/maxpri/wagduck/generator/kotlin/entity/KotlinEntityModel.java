package org.maxpri.wagduck.generator.kotlin.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinEntityModel {
    private String packageName; // e.g., org.maxpri.wagduck.domain.model
    private String className;  // e.g., User
    private String tableName;  // e.g., users or tbl_users
    private String description; // Class KDoc

    private boolean includeAuditing; // Flag to include @EntityListeners and auditing fields
    // No useLombok, Kotlin data classes or regular classes handle this.
    // We will generate an 'open class' for better JPA compatibility.

    private Set<String> imports; // All necessary imports
    private List<String> classAnnotations; // e.g., "@Entity", "@Table(name = "users")", "@EntityListeners"

    private List<KotlinAttributeModel> attributes;
    private List<KotlinRelationshipModel> relationships;

    private KotlinAttributeModel createdAtAttribute; // For auditing
    private KotlinAttributeModel updatedAtAttribute; // For auditing
}