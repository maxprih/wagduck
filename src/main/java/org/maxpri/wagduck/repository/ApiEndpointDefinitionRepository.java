package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.ApiEndpointDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiEndpointDefinitionRepository extends JpaRepository<ApiEndpointDefinition, UUID> {
}