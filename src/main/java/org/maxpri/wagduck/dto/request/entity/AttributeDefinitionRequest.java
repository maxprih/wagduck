package org.maxpri.wagduck.dto.request.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttributeDefinitionRequest {
    @NotNull
    private UUID entityId;

    @NotBlank
    @Size(max = 100)
    private String attributeName;

    @NotBlank
    @Size(max = 100)
    private String dataType;

    @Size(max = 100)
    private String columnName;

    @Size(max = 1000)
    private String description;

    private Boolean isPrimaryKey = false;
    private Boolean isRequired = false;
    private Boolean isUnique = false;
    private Boolean isIndexed = false;

    private Integer length;
    private Integer precision;
    private Integer scale;

    @Size(max = 255)
    private String defaultValue;
}