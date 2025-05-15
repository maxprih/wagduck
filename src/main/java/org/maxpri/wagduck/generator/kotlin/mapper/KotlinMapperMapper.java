package org.maxpri.wagduck.generator.kotlin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface KotlinMapperMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage() + \".mapper\")")
    @Mapping(target = "mapperName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"Mapper\")")
    @Mapping(target = "entityClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "entityClassImport", expression = "java(config.getBasePackage() + \".domain.model.\" + org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()))")
    @Mapping(target = "requestDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"RequestDto\")")
    @Mapping(target = "requestDtoClassImport", expression = "java(config.getBasePackage() + \".dto.\" + org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"RequestDto\")")
    @Mapping(target = "responseDtoClassName", expression = "java(org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"ResponseDto\")")
    @Mapping(target = "responseDtoClassImport", expression = "java(config.getBasePackage() + \".dto.\" + org.maxpri.wagduck.util.NamingUtils.toPascalCase(entity.getEntityName()) + \"ResponseDto\")")
    @Mapping(target = "imports", source = "entity", qualifiedByName = "collectMapperImports")
    @Mapping(target = "usesMapperNames", source = "entity", qualifiedByName = "collectUsedMapperNames")
    KotlinMapperModel toKotlinEntityDtoMapperDefModel(ProjectConfiguration config, EntityDefinition entity);

    @Named("collectMapperImports")
    default Set<String> collectMapperImports(EntityDefinition entity) {
        Set<String> imports = new HashSet<>();
        imports.add("org.mapstruct.Mapper");
        imports.add("org.mapstruct.Mapping");
        imports.add("org.mapstruct.MappingTarget");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".domain.model." + NamingUtils.toPascalCase(entity.getEntityName()));
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "RequestDto");
        imports.add(entity.getProjectConfiguration().getBasePackage() + ".dto." + NamingUtils.toPascalCase(entity.getEntityName()) + "ResponseDto");

        return imports;
    }

    @Named("collectUsedMapperNames")
    default List<String> collectUsedMapperNames(EntityDefinition currentEntity) {
        Set<String> relatedEntityNames = new HashSet<>();
        if (currentEntity.getRelationships() != null) {
            currentEntity.getRelationships().stream()
                    .map(RelationshipDefinition::getTargetEntity)
                    .filter(Objects::nonNull)
                    .map(EntityDefinition::getEntityName)
                    .forEach(relatedEntityNames::add);
        }
        for (EntityDefinition otherEntity : currentEntity.getProjectConfiguration().getEntities()) {
            if (otherEntity.equals(currentEntity) || otherEntity.getRelationships() == null) {
                continue;
            }
            for (RelationshipDefinition relOnOther : otherEntity.getRelationships()) {
                if (relOnOther.getTargetEntity() != null &&
                        relOnOther.getTargetEntity().getEntityName().equals(currentEntity.getEntityName()) &&
                        relOnOther.getTargetFieldName() != null && !relOnOther.getTargetFieldName().isBlank()) {
                    relatedEntityNames.add(otherEntity.getEntityName());
                }
            }
        }

        return relatedEntityNames.stream()
                .map(NamingUtils::toPascalCase)
                .map(name -> name + "Mapper")
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}