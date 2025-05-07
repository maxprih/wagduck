package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntityDefinitionRepository extends JpaRepository<EntityDefinition, UUID> {
    List<EntityDefinition> findByProjectConfiguration_Id(UUID projectId);
}