package org.maxpri.wagduck.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.dto.request.entity.AttributeDefinitionRequest;
import org.maxpri.wagduck.dto.response.entity.AttributeDefinitionResponse;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

    @Mappings({
            @Mapping(source = "isPrimaryKey", target = "primaryKey"),
            @Mapping(source = "isUnique", target = "unique"),
            @Mapping(source = "isRequired", target = "required")
    })
    AttributeDefinition requestToEntity(AttributeDefinitionRequest request);

    @Mappings({
            @Mapping(source = "primaryKey", target = "isPrimaryKey"),
            @Mapping(source = "unique", target = "isUnique"),
            @Mapping(source = "required", target = "isRequired")
    })
    AttributeDefinitionResponse entityToResponse(AttributeDefinition entity);

    void updateEntityFromRequest(@MappingTarget AttributeDefinition existingEntity, AttributeDefinitionRequest request);
}
