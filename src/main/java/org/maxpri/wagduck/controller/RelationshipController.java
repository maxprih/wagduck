package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.dto.request.entity.RelationshipDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.RelationshipDefinitionResponse;
import org.maxpri.wagduck.service.RelationshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/relationships")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;

    @GetMapping("/{relationshipId}")
    public ResponseEntity<RelationshipDefinitionResponse> getRelationship(@PathVariable UUID relationshipId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(relationshipService.findRelationshipById(relationshipId));
    }

    @GetMapping
    public ResponseEntity<List<RelationshipDefinitionResponse>> getAllRelationshipsForEntity(@RequestParam UUID entityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(relationshipService.findRelationshipsByEntityId(entityId));
    }

    @PostMapping
    public ResponseEntity<RelationshipDefinitionResponse> createRelationship(
            @RequestBody RelationshipDefinitionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(relationshipService.createRelationship(request, UUID.fromString("1cd294c9-0c04-4c21-b8a1-53a82dfb470e")));
    }

    @PutMapping("/{relationshipId}")
    public ResponseEntity<RelationshipDefinitionResponse> updateRelationship(
            @PathVariable UUID relationshipId,
            @RequestBody RelationshipDefinitionRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(relationshipService.updateRelationship(relationshipId, request));
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> deleteRelationship(@PathVariable UUID relationshipId) {
        relationshipService.deleteRelationship(relationshipId);
        return ResponseEntity.noContent().build();
    }
}
