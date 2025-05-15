package org.maxpri.wagduck.mapper;

import org.mapstruct.Mapper;
import org.maxpri.wagduck.domain.entity.ApiEndpointDefinition;
import org.maxpri.wagduck.dto.response.entity.ApiEndpointDefinitionResponse;

@Mapper(componentModel = "spring")
public interface ApiEndpointMapper {

    ApiEndpointDefinitionResponse entityToResponse(ApiEndpointDefinition entity);
}
