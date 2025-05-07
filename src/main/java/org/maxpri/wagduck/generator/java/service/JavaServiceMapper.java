package org.maxpri.wagduck.generator.java.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaServiceMapper {

    @Mapping(target = "basePackage", expression = "java(config.getBasePackage())")
    @Mapping(target = "servicePackage", expression = "java(config.getBasePackage() + \".service\")")
    @Mapping(target = "repositoryPackage", expression = "java(config.getBasePackage() + \".repository\")")
    @Mapping(target = "dtoPackage", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "entityPackage", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "exceptionPackage", expression = "java(config.getBasePackage() + \".exception\")")
    @Mapping(target = "serviceClassName", expression = "java(entity.getEntityName() + \"Service\")")
    @Mapping(target = "entityClassName", expression = "java(entity.getEntityName())")
    @Mapping(target = "repositoryClassName", expression = "java(entity.getEntityName() + \"Repository\")")
    @Mapping(target = "requestDtoClassName", expression = "java(entity.getEntityName() + \"Request\")")
    @Mapping(target = "responseDtoClassName", expression = "java(entity.getEntityName() + \"Response\")")
    @Mapping(target = "entityMapperName", expression = "java(entity.getEntityName() + \"Mapper\")")
    @Mapping(target = "repositoryVariableName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Repository\")")
    @Mapping(target = "entityMapperVariableName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Mapper\")")
    @Mapping(target = "primaryKeyType", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyType(entity))")
    @Mapping(target = "primaryKeyName", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyName(entity))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectServiceImports")
    @Mapping(target = "resourceNotFoundExceptionName", expression = "java(entity.getEntityName() + \"NotFoundException\")")
    JavaServiceModel toJavaServiceModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("collectServiceImports")
    default Set<String> collectServiceImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        ProjectConfiguration config = entity.getProjectConfiguration();
        String basePackage = config.getBasePackage();
        String pkType = NamingUtils.findPrimaryKeyType(entity);
        String dtoPackage = basePackage + ".dto";

        imports.add(dtoPackage + "." + entity.getEntityName() + "Request");
        imports.add(dtoPackage + "." + entity.getEntityName() + "Response");

        imports.add("java.util.List");
        imports.add("java.util.Optional");

        imports.add(basePackage + ".domain.model." + entity.getEntityName());
        imports.add(basePackage + ".repository." + entity.getEntityName() + "Repository");
        imports.add(basePackage + ".mapper." + entity.getEntityName() + "Mapper");
        imports.add(basePackage + ".exception." + entity.getEntityName() + "NotFoundException");

        imports.add("org.springframework.stereotype.Service");
        imports.add("org.springframework.transaction.annotation.Transactional");
        imports.add("lombok.RequiredArgsConstructor");
        imports.add("lombok.extern.slf4j.Slf4j");

        if ("UUID".equals(pkType)) {
            imports.add("java.util.UUID");
        } else if (pkType.contains(".")) {
            imports.add(pkType);
        }

        return imports;
    }
}