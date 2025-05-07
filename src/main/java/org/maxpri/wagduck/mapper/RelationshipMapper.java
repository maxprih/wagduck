package org.maxpri.wagduck.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.dto.request.entity.RelationshipDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.RelationshipDefinitionResponse;

@Mapper(componentModel = "spring")
public interface RelationshipMapper {

    @Mapping(target = "sourceEntity.id", source = "sourceEntityId")
    @Mapping(target = "targetEntity.id", source = "targetEntityId")
    RelationshipDefinition requestToEntity(RelationshipDefinitionRequest request);

    @Mapping(target = "sourceEntityId", source = "sourceEntity.id")
    @Mapping(target = "targetEntityId", source = "targetEntity.id")
    RelationshipDefinitionResponse entityToResponse(RelationshipDefinition entity);
}
