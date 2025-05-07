package org.maxpri.wagduck.dto.response.entity;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttributeDefinitionResponse {
    private UUID id;
    private String attributeName;
    private String dataType;
    private String columnName;
    private String description;
    private Boolean isPrimaryKey;
    private Boolean isRequired;
    private Boolean isUnique;
    private Boolean isIndexed;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String defaultValue;
}