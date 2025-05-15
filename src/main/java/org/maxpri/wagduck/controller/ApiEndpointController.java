package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.dto.request.entity.ApiEndpointDefinitionUpdateRequest;
import org.maxpri.wagduck.dto.response.entity.ApiEndpointDefinitionResponse;
import org.maxpri.wagduck.service.ApiEndpointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/entity/{entityId}/api-endpoints")
@RequiredArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    @PutMapping
    public ResponseEntity<List<ApiEndpointDefinitionResponse>> updateApiEndpoints(
            @PathVariable UUID entityId,
            @RequestBody ApiEndpointDefinitionUpdateRequest updateRequest) {
        List<ApiEndpointDefinitionResponse> updatedEndpoints =
                apiEndpointService.updateApiEndPointsForEntity(updateRequest, entityId);
        return ResponseEntity.ok(updatedEndpoints);
    }

    @GetMapping
    public ResponseEntity<List<ApiEndpointDefinitionResponse>> getApiEndpoints(
            @PathVariable UUID entityId) {
        List<ApiEndpointDefinitionResponse> endpoints =
                apiEndpointService.getApiEndpointsForEntity(entityId);
        return ResponseEntity.ok(endpoints);
    }
}