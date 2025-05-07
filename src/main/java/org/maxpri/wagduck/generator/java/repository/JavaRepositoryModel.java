package org.maxpri.wagduck.generator.java.repository;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JavaRepositoryModel {
    private String packageName;         // e.g., org.maxpri.wagduck.repository
    private String interfaceName;       // e.g., UserRepository
    private String entityClassName;     // e.g., User
    private String entityPackage;       // e.g., org.maxpri.wagduck.domain.model
    private String primaryKeyType;      // e.g., Long, UUID, String
    private Set<String> imports;        // Imports needed (Entity, PK Type, JpaRepository, Repository)
}