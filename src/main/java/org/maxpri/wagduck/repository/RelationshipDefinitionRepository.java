package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RelationshipDefinitionRepository extends JpaRepository<RelationshipDefinition, UUID> {
    List<RelationshipDefinition> findBySourceEntity_Id(UUID sourceEntityId);
}