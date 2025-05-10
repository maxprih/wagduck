package org.maxpri.wagduck.generator.kotlin.repository;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class KotlinRepositoryModel {
    private String packageName;         // e.g., org.maxpri.wagduck.repository
    private Set<String> imports;        // Imports for JpaRepository, Entity, PK type
    private String repositoryName;      // e.g., UserRepository
    private String entityClassName;     // e.g., User
    private String entityClassImport;   // e.g., org.maxpri.wagduck.domain.model.User
    private String primaryKeyType;      // e.g., Long, String, java.util.UUID (the Kotlin type)
}