package org.maxpri.wagduck.service;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.domain.entity.ApiEndpointDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.enums.ApiMethod;
import org.maxpri.wagduck.dto.request.entity.ApiEndpointDefinitionUpdateRequest;
import org.maxpri.wagduck.dto.response.entity.ApiEndpointDefinitionResponse;
import org.maxpri.wagduck.mapper.ApiEndpointMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiEndpointService {
    private final EntityService entityService;
    private final ApiEndpointMapper apiEndpointMapper;

    @Transactional
    public List<ApiEndpointDefinitionResponse> updateApiEndPointsForEntity(
            ApiEndpointDefinitionUpdateRequest updateRequest, UUID entityId) {

        EntityDefinition entityDefinition = entityService.findEntityByIdInternal(entityId);

        Map<ApiMethod, ApiEndpointDefinition> currentEndpointsMap =
                entityDefinition.getApiEndpoints().stream()
                        .collect(Collectors.toMap(ApiEndpointDefinition::getApiMethod, Function.identity()));

        Map<ApiMethod, Boolean> desiredStates = updateRequest.getMethods();

        for (ApiMethod method : ApiMethod.values()) {
            boolean shouldExist = desiredStates.getOrDefault(method, false);
            ApiEndpointDefinition existingEndpoint = currentEndpointsMap.get(method);

            if (shouldExist) {
                if (existingEndpoint == null) {
                    ApiEndpointDefinition newEndpoint = new ApiEndpointDefinition();
                    newEndpoint.setEntityDefinition(entityDefinition);
                    newEndpoint.setApiMethod(method);
                    newEndpoint.setHttpPath(generateHttpPath(entityDefinition.getEntityName(), method));

                    entityDefinition.getApiEndpoints().add(newEndpoint);
                }
            } else {
                if (existingEndpoint != null) {
                    entityDefinition.getApiEndpoints().remove(existingEndpoint);
                }
            }
        }

        return entityDefinition.getApiEndpoints().stream()
                .map(apiEndpointMapper::entityToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ApiEndpointDefinitionResponse> getApiEndpointsForEntity(UUID entityId) {
        EntityDefinition entityDefinition = entityService.findEntityByIdInternal(entityId);
        return entityDefinition.getApiEndpoints().stream()
                .map(apiEndpointMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    private String generateHttpPath(String entityName, ApiMethod method) {
        String sanitizedEntityName = sanitizeEntityNameForPath(entityName);
        String basePath = "/api/v1/" + sanitizedEntityName;
        String idPlaceholder = "/{id}";

        switch (method) {
            case GET:
            case PUT:
            case DELETE:
                return basePath + idPlaceholder;
            case GET_LIST:
            case POST:
                return basePath;
            default:
                throw new IllegalArgumentException("Unsupported API method for path generation: " + method);
        }
    }

    private String sanitizeEntityNameForPath(String entityName) {
        if (entityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity name cannot be empty or just whitespace for path generation.");
        }
        return entityName.trim().replaceAll("\\s+", "-").toLowerCase();
    }
}
