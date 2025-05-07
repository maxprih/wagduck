package org.maxpri.wagduck.dto.response.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.RelationshipType;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class RelationshipDefinitionResponse {
    private UUID id;
    private UUID sourceEntityId;
    private String sourceFieldName;
    private UUID targetEntityId;
    private RelationshipType relationshipType;
    private String targetFieldName;
    private Boolean owningSide;
    private FetchType fetchType;
    private Set<CascadeType> cascadeTypes;
    private String description;
    private String joinColumnName;
    private String joinTableName;
    private String joinTableSourceColumnName;
    private String joinTableTargetColumnName;
}
