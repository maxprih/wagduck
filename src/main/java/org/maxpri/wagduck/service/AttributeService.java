package org.maxpri.wagduck.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.dto.request.entity.AttributeDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.AttributeDefinitionResponse;
import org.maxpri.wagduck.exception.DuplicateAttributeException;
import org.maxpri.wagduck.exception.PrimaryKeyAlreadyExistsException;
import org.maxpri.wagduck.mapper.AttributeMapper;
import org.maxpri.wagduck.repository.AttributeDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.AttributeNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final AttributeDefinitionRepository attributeRepository;
    private final AttributeMapper attributeMapper;
    private final EntityService entityService;

    @Transactional
    public AttributeDefinitionResponse createAttribute(AttributeDefinitionRequest request) {
        EntityDefinition entity = entityService.findEntityByIdInternal(request.getEntityId());

        entity.getAttributes().stream()
                .filter(a -> a.getAttributeName().equalsIgnoreCase(request.getAttributeName()))
                .findAny()
                .ifPresent(a -> {
                    throw new DuplicateAttributeException(
                            "Attribute already exists: " + request.getAttributeName());
                });

        if (request.getIsPrimaryKey()) {
            entity.getAttributes().stream()
                    .filter(AttributeDefinition::isPrimaryKey)
                    .findAny()
                    .ifPresent(attribute -> {
                        throw new PrimaryKeyAlreadyExistsException(
                                "Primary key already exists for entity: " + entity.getEntityName());
                    });
        }

        AttributeDefinition attribute = attributeMapper.requestToEntity(request);
        attribute.setEntityDefinition(entity);

        return attributeMapper.entityToResponse(attributeRepository.save(attribute));
    }

    public List<AttributeDefinitionResponse> findAttributesByEntityId(UUID entityId) {
        return attributeRepository.findByEntityDefinition_Id(entityId).stream()
                .map(attributeMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public AttributeDefinitionResponse updateAttribute(UUID attributeId, AttributeDefinitionRequest request) {
        AttributeDefinition attribute = findAttributeById(attributeId);

        attributeMapper.updateEntityFromRequest(attribute, request);
        return attributeMapper.entityToResponse(attributeRepository.save(attribute));
    }

    public void deleteAttribute(UUID attributeId) {
        findAttributeById(attributeId);
        attributeRepository.deleteById(attributeId);
    }

    @SneakyThrows
    private AttributeDefinition findAttributeById(UUID attributeId) {
        return attributeRepository.findById(attributeId)
                .orElseThrow(() -> new AttributeNotFoundException("Attribute not found with id: " + attributeId));
    }
}
