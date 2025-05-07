package org.maxpri.wagduck.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.dto.request.entity.EntityDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.EntityDefinitionResponse;

@Mapper(componentModel = "spring")
public interface EntityMapper {

    EntityDefinition requestToEntity(EntityDefinitionRequest request);

    EntityDefinitionResponse entityToResponse(EntityDefinition entity);

    void updateEntityFromRequest(@MappingTarget EntityDefinition existingEntity, EntityDefinitionRequest request);
}
