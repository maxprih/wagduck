package org.maxpri.wagduck.generator.java.repository;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaRepositoryMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".repository\")")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Repository\")")
    @Mapping(target = "entityClassName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackage", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "primaryKeyType", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyType(entity))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectRepositoryImports")
    JavaRepositoryModel toJavaRepositoryModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("collectRepositoryImports")
    default Set<String> collectRepositoryImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        ProjectConfiguration config = entity.getProjectConfiguration();

        imports.add("org.springframework.data.jpa.repository.JpaRepository");
        imports.add("org.springframework.stereotype.Repository");
        imports.add(config.getBasePackage() + ".domain.model." + entity.getEntityName());
        String pkType = NamingUtils.findPrimaryKeyType(entity);
        if ("UUID".equals(pkType)) {
            imports.add("java.util.UUID");
        } else if (pkType.contains(".")) {
            imports.add(pkType);
        }

        return imports;
    }
}