package org.maxpri.wagduck.generator.java.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaMapperMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".mapper\")")
    @Mapping(target = "interfaceName", expression = "java(entity.getEntityName() + \"Mapper\")")
    @Mapping(target = "componentModel", expression = "java(\"spring\")")
    @Mapping(target = "entityClassName", expression = "java(entity.getEntityName())")
    @Mapping(target = "entityPackage", expression = "java(config.getBasePackage() + \".domain.model\")")
    @Mapping(target = "requestDtoClassName", expression = "java(entity.getEntityName() + \"Request\")")
    @Mapping(target = "responseDtoClassName", expression = "java(entity.getEntityName() + \"Response\")")
    @Mapping(target = "dtoPackage", expression = "java(config.getBasePackage() + \".dto\")")
    @Mapping(target = "primaryKeyType", expression = "java(org.maxpri.wagduck.util.NamingUtils.findPrimaryKeyType(entity))")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectMapperImports")
    @Mapping(target = "toDtoMappings", expression = "java(new java.util.ArrayList<String>())") // Start empty, add specific later if needed
    @Mapping(target = "toEntityMappings", expression = "java(generateToEntityMappings(entity))")
    @Mapping(target = "updateMappings", expression = "java(generateUpdateMappings(entity))")
    JavaMapperModel toJavaMapperModel(ProjectConfiguration config, EntityDefinition entity);


    @Named("collectMapperImports")
    default Set<String> collectMapperImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        ProjectConfiguration config = entity.getProjectConfiguration();
        String basePackage = config.getBasePackage();
        String pkType = NamingUtils.findPrimaryKeyType(entity);

        imports.add("org.mapstruct.*");
        imports.add(basePackage + ".domain.model." + entity.getEntityName());
        imports.add(basePackage + ".dto." + entity.getEntityName() + "Request");
        imports.add(basePackage + ".dto." + entity.getEntityName() + "Response");

        if ("UUID".equals(pkType)) {
            imports.add("java.util.UUID");
        } else if (pkType.contains(".")) {
            imports.add(pkType);
        }

        return imports;
    }

    default List<String> generateToEntityMappings(EntityDefinition entity) {
        List<String> mappings = new ArrayList<>();
        String pkName = NamingUtils.findPrimaryKeyName(entity);
        mappings.add(String.format("@Mapping(target = \"%s\", ignore = true)", pkName));
        entity.getAttributes().stream()
              .filter(NamingUtils::isAuditAttribute)
              .forEach(attr -> mappings.add(String.format("@Mapping(target = \"%s\", ignore = true)", NamingUtils.toCamelCase(attr.getAttributeName()))));

        return mappings;
    }

    default List<String> generateUpdateMappings(EntityDefinition entity) {
        List<String> mappings = new ArrayList<>();
        String pkName = NamingUtils.findPrimaryKeyName(entity);
        mappings.add(String.format("@Mapping(target = \"%s\", ignore = true)", pkName));
        entity.getAttributes().stream()
                .filter(NamingUtils::isAuditAttribute)
                .forEach(attr -> mappings.add(String.format("@Mapping(target = \"%s\", ignore = true)", NamingUtils.toCamelCase(attr.getAttributeName()))));
        return mappings;
    }
}
