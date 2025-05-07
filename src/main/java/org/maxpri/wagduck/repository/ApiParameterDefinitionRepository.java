package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.ApiParameterDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiParameterDefinitionRepository extends JpaRepository<ApiParameterDefinition, UUID> {
}