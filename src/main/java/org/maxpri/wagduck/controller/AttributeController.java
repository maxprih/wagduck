package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.dto.request.entity.AttributeDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.AttributeDefinitionResponse;
import org.maxpri.wagduck.service.AttributeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/attribute")
@RequiredArgsConstructor
public class AttributeController {
    private final AttributeService attributeService;

    @PostMapping
    public ResponseEntity<AttributeDefinitionResponse> createAttribute(
            @RequestBody AttributeDefinitionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(attributeService.createAttribute(request));
    }

    @GetMapping
    public ResponseEntity<List<AttributeDefinitionResponse>> getAttributesByEntityId(@RequestParam UUID entityId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(attributeService.findAttributesByEntityId(entityId));
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<AttributeDefinitionResponse> updateAttribute(
            @PathVariable UUID attributeId,
            @RequestBody AttributeDefinitionRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(attributeService.updateAttribute(attributeId, request));
    }

    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable UUID attributeId) {
        attributeService.deleteAttribute(attributeId);
        return ResponseEntity.noContent().build();
    }
}
