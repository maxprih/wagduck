package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.dto.request.entity.EntityDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.EntityDefinitionResponse;
import org.maxpri.wagduck.service.EntityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/entity")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EntityController {
    private final EntityService entityService;

    @GetMapping("/{entityId}")
    public ResponseEntity<EntityDefinitionResponse> getEntity(@PathVariable UUID entityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(entityService.findEntityById(entityId));
    }

    @GetMapping
    public ResponseEntity<List<EntityDefinitionResponse>> getAllEntitiesForProject(@RequestParam UUID projectId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(entityService.findEntitiesForProject(projectId));
    }

    @PostMapping
    public ResponseEntity<EntityDefinitionResponse> createEntity(
            @RequestBody EntityDefinitionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entityService.createEntity(request, UUID.fromString(jwt.getSubject())));
    }

    @PutMapping("/{entityId}")
    public ResponseEntity<EntityDefinitionResponse> updateEntity(
            @PathVariable UUID entityId,
            @RequestBody EntityDefinitionRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(entityService.updateEntity(entityId, request));
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> deleteEntity(@PathVariable UUID entityId) {
        entityService.deleteEntity(entityId);
        return ResponseEntity.noContent().build();
    }
}
