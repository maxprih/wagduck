package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {
    List<AttributeDefinition> findByEntityDefinition_Id(UUID entityId);
}
