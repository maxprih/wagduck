package org.maxpri.wagduck.service;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.dto.request.entity.EntityDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.EntityDefinitionResponse;
import org.maxpri.wagduck.exception.DuplicateEntityException;
import org.maxpri.wagduck.exception.ProjectNotFoundException;
import org.maxpri.wagduck.mapper.EntityMapper;
import org.maxpri.wagduck.repository.EntityDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntityService {
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityMapper entityMapper;
    private final ProjectService projectService;

    public List<EntityDefinitionResponse> findEntitiesForProject(UUID projectId) {
        return entityDefinitionRepository.findByProjectConfiguration_Id(projectId).stream()
                .map(entityMapper::entityToResponse)
                .toList();
    }

    public EntityDefinitionResponse createEntity(EntityDefinitionRequest request, UUID ownerId) {
        ProjectConfiguration project = projectService.findProjectByIdAndOwnerInternal(request.getProjectId(), ownerId);

        project.getEntities().stream()
                .filter(e -> e.getEntityName().equalsIgnoreCase(request.getEntityName()))
                .findAny()
                .ifPresent(a -> {
                    throw new DuplicateEntityException(
                            "Entity already exists: " + request.getEntityName());
                });

        EntityDefinition entity = entityMapper.requestToEntity(request);
        entity.setProjectConfiguration(project);

        return entityMapper.entityToResponse(entityDefinitionRepository.save(entity));
    }

    public EntityDefinitionResponse findEntityById(UUID entityId) {
        return entityDefinitionRepository.findById(entityId)
                .map(entityMapper::entityToResponse)
                .orElseThrow(() -> new ProjectNotFoundException("Entity not found with id: " + entityId));
    }

    public EntityDefinition findEntityByIdInternal(UUID entityId) {
        return entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new ProjectNotFoundException("Entity not found with id: " + entityId));
    }

    public EntityDefinitionResponse updateEntity(UUID entityId, EntityDefinitionRequest request) {
        EntityDefinition entity = findEntityByIdInternal(entityId);
        entityMapper.updateEntityFromRequest(entity, request);
        return entityMapper.entityToResponse(entityDefinitionRepository.save(entity));
    }

    public void deleteEntity(UUID entityId) {
        if (!entityDefinitionRepository.existsById(entityId)) {
            throw new ProjectNotFoundException("Entity not found with id: " + entityId);
        }
        entityDefinitionRepository.deleteById(entityId);
    }
}
