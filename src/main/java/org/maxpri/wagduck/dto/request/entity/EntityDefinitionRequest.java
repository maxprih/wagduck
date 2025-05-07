package org.maxpri.wagduck.dto.request.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EntityDefinitionRequest {
    @NotNull
    private UUID projectId;

    @NotBlank
    @Size(max = 100)
    private String entityName;

    @Size(max = 100)
    private String tableName;
}