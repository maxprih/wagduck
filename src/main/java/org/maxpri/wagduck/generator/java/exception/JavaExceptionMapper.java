package org.maxpri.wagduck.generator.java.exception;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;

@Mapper(componentModel = "spring")
public interface JavaExceptionMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".exception\")")
    @Mapping(target = "exceptionName", expression = "java(entity.getEntityName() + \"NotFoundException\")")
    JavaExceptionModel toJavaExceptionModel(ProjectConfiguration config, EntityDefinition entity);
}
