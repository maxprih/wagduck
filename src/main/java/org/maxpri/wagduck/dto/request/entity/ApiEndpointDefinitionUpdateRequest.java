package org.maxpri.wagduck.dto.request.entity;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.ApiMethod;

import java.util.Map;

@Data
@Builder
public class ApiEndpointDefinitionUpdateRequest {
    private Map<ApiMethod, Boolean> methods;
}
