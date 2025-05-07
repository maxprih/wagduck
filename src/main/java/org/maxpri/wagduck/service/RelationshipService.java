package org.maxpri.wagduck.service;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.dto.request.entity.RelationshipDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.RelationshipDefinitionResponse;
import org.maxpri.wagduck.exception.DuplicateRelationshipException;
import org.maxpri.wagduck.exception.RelationshipNotFoundException;
import org.maxpri.wagduck.mapper.RelationshipMapper;
import org.maxpri.wagduck.repository.RelationshipDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private final RelationshipDefinitionRepository relationshipRepository;
    private final RelationshipMapper relationshipMapper;
    private final EntityService entityService;

    public List<RelationshipDefinitionResponse> findRelationshipsByEntityId(UUID entityId) {
        return relationshipRepository.findBySourceEntity_Id(entityId).stream()
                .map(relationshipMapper::entityToResponse)
                .toList();
    }

    public RelationshipDefinitionResponse findRelationshipById(UUID relationshipId) {
        return relationshipRepository.findById(relationshipId)
                .map(relationshipMapper::entityToResponse)
                .orElseThrow(() -> new RelationshipNotFoundException("Relationship not found with id: " + relationshipId));
    }

    @Transactional
    public RelationshipDefinitionResponse createRelationship(RelationshipDefinitionRequest request, UUID ownerId) {
        EntityDefinition sourceEntity = entityService.findEntityByIdInternal(request.getSourceEntityId());

        sourceEntity.getRelationships().stream()
                .filter(r -> r.getSourceFieldName().equalsIgnoreCase(request.getSourceFieldName()))
                .findAny()
                .ifPresent(r -> {
                    throw new DuplicateRelationshipException(
                            "Relationship with field name already exists: " + request.getSourceFieldName());
                });

        RelationshipDefinition relationship = relationshipMapper.requestToEntity(request);
        EntityDefinition targetEntity = entityService.findEntityByIdInternal(request.getTargetEntityId());
        relationship.setSourceEntity(sourceEntity);
        relationship.setTargetEntity(targetEntity);

        // Сохраняем основную связь
        RelationshipDefinition saved = relationshipRepository.save(relationship);

        // Если задано targetFieldName — создаём обратную связь
        if (request.getTargetFieldName() != null && !request.getTargetFieldName().isBlank()) {
            RelationshipDefinition inverse = new RelationshipDefinition();
            inverse.setSourceEntity(targetEntity);
            inverse.setSourceFieldName(request.getTargetFieldName());
            inverse.setTargetEntity(sourceEntity);
            inverse.setTargetFieldName(request.getSourceFieldName());
            inverse.setFetchType(relationship.getFetchType());

            // Определяем тип и владельца для обратной стороны
            switch (relationship.getRelationshipType()) {
                case ONE_TO_MANY -> {
                    inverse.setRelationshipType(org.maxpri.wagduck.domain.enums.RelationshipType.MANY_TO_ONE);
                    inverse.setOwningSide(true);
                    inverse.setJoinColumnName(relationship.getJoinColumnName());
                }
                case MANY_TO_ONE -> {
                    inverse.setRelationshipType(org.maxpri.wagduck.domain.enums.RelationshipType.ONE_TO_MANY);
                    inverse.setOwningSide(false);
                    inverse.setJoinColumnName(relationship.getJoinColumnName());
                }
                case ONE_TO_ONE -> {
                    inverse.setRelationshipType(org.maxpri.wagduck.domain.enums.RelationshipType.ONE_TO_ONE);
                    inverse.setOwningSide(!relationship.isOwningSide());
                    inverse.setJoinColumnName(relationship.getJoinColumnName());
                }
                case MANY_TO_MANY -> {
                    inverse.setRelationshipType(org.maxpri.wagduck.domain.enums.RelationshipType.MANY_TO_MANY);
                    inverse.setOwningSide(false);
                    inverse.setJoinTableName(relationship.getJoinTableName());
                    inverse.setJoinTableSourceColumnName(relationship.getJoinTableTargetColumnName());
                    inverse.setJoinTableTargetColumnName(relationship.getJoinTableSourceColumnName());
                }
            }
            relationshipRepository.save(inverse);
        }

        return relationshipMapper.entityToResponse(saved);
    }
    
    @Transactional
    public void deleteRelationship(UUID relationshipId) {
        if (!relationshipRepository.existsById(relationshipId)) {
            throw new RelationshipNotFoundException("Relationship not found with id: " + relationshipId);
        }
        relationshipRepository.deleteById(relationshipId);
    }

    @Transactional
    public RelationshipDefinitionResponse updateRelationship(UUID relationshipId, RelationshipDefinitionRequest request) {
        RelationshipDefinition relationship = findRelationshipByIdInternal(relationshipId);
        relationship.setSourceFieldName(request.getSourceFieldName());
        // обновить другие поля по необходимости
        // при необходимости обновить sourceEntity/targetEntity
        return relationshipMapper.entityToResponse(relationshipRepository.save(relationship));
    }

    public RelationshipDefinition findRelationshipByIdInternal(UUID relationshipId) {
        return relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RelationshipNotFoundException("Relationship not found with id: " + relationshipId));
    }
}
