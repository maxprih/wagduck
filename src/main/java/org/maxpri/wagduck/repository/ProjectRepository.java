package org.maxpri.wagduck.repository;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectConfiguration, UUID> {
    Optional<ProjectConfiguration> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<ProjectConfiguration> findByOwnerId(UUID ownerId);
}