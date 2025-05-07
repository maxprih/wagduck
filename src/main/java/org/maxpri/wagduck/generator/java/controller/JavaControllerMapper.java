package org.maxpri.wagduck.generator.java.controller;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaControllerMapper {

    @Mapping(target = "controllerPackage", expression = "java(config.getBasePackage() + \".controller\")")
    @Mapping(target = "entityPackage", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "entityClassName", expression = "java(entity.getEntityName())")
    @Mapping(target = "serviceClassName", expression = "java(entity.getEntityName() + \"Service\")")
    @Mapping(target = "serviceVariableName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()) + \"Service\")")
    @Mapping(target = "requestDtoClassName", expression = "java(entity.getEntityName() + \"Request\")")
    @Mapping(target = "responseDtoClassName", expression = "java(entity.getEntityName() + \"Response\")")
    @Mapping(target = "primaryKeyType", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyType(entity))")
    @Mapping(target = "primaryKeyName", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyName(entity))")
    @Mapping(target = "basePath", expression = "java(\"/\" + org.maxpri.wagduck.util.NamingUtils.toCamelCase(entity.getEntityName()))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "generateControllerImports")
    JavaControllerModel toJavaControllerModel(ProjectConfiguration config, EntityDefinition entity);


    @Named("generateControllerImports")
    default Set<String> generateControllerImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("org.springframework.http.ResponseEntity");
        imports.add("org.springframework.web.bind.annotation.*");
        imports.add("org.springframework.http.HttpStatus");
        imports.add("java.util.List");
        imports.add("lombok.RequiredArgsConstructor");

        String dtoPackage = entity.getProjectConfiguration().getBasePackage() + ".dto";
        imports.add(dtoPackage + "." + entity.getEntityName() + "Request");
        imports.add(dtoPackage + "." + entity.getEntityName() + "Response");

        String servicePackage = entity.getProjectConfiguration().getBasePackage() + ".service";
        imports.add(servicePackage + "." + entity.getEntityName() + "Service");

        String pkType = NamingUtils.findPrimaryKeyType(entity);
        if ("UUID".equals(pkType)) {
            imports.add("java.util.UUID");
        }

        return imports;
    }
}