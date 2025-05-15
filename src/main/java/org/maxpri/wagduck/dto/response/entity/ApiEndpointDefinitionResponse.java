package org.maxpri.wagduck.dto.response.entity;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.ApiMethod;

import java.util.UUID;

@Data
@Builder
public class ApiEndpointDefinitionResponse {
    private UUID id;
    private String httpPath;
    private ApiMethod apiMethod;
}
