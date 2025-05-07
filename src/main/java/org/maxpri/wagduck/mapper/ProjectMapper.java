package org.maxpri.wagduck.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.dto.request.project.ProjectConfigurationCreateRequest;
import org.maxpri.wagduck.dto.response.project.ProjectConfigurationResponse;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectConfiguration requestToEntity(ProjectConfigurationCreateRequest request);

    ProjectConfigurationResponse entityToResponse(ProjectConfiguration entity);

    void updateEntityFromRequest(@MappingTarget ProjectConfiguration existingEntity, ProjectConfigurationCreateRequest request);
}
