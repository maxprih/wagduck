package org.maxpri.wagduck.dto.response.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EntityDefinitionResponse {
    private UUID id;
    private String entityName;
    private String tableName;
    private String description;
    private List<AttributeDefinitionResponse> attributes;
    private List<RelationshipDefinitionResponse> relationships;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}