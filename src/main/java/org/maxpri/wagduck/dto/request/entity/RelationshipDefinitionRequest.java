package org.maxpri.wagduck.dto.request.entity;

import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.RelationshipType;

import java.util.UUID;

@Data
@Builder
public class RelationshipDefinitionRequest {

    @NotNull
    private UUID sourceEntityId;

    @NotBlank
    @Size(max = 100)
    private String sourceFieldName;

    @NotNull
    private UUID targetEntityId;

    @Size(max = 100)
    private String targetFieldName;

    @NotNull
    private RelationshipType relationshipType;

    @NotNull
    private Boolean owningSide = true;

    private FetchType fetchType = FetchType.LAZY;

    @Size(max = 100)
    private String joinColumnName;
    @Size(max = 100)
    private String joinTableName;
    @Size(max = 100)
    private String joinTableSourceColumnName;
    @Size(max = 100)
    private String joinTableTargetColumnName;
}