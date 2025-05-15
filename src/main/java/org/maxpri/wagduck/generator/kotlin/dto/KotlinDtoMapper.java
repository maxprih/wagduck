package org.maxpri.wagduck.generator.kotlin.dto;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
import org.maxpri.wagduck.domain.enums.ProjectOptions;
import org.maxpri.wagduck.domain.enums.RelationshipType;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface KotlinDtoMapper {

    @Named("buildDtoDefModelInternal")
    default KotlinDtoModel buildDtoDefModel(ProjectConfiguration config,
                                            EntityDefinition entity,
                                            boolean isResponseDto) {
        String dtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
        String dtoClassName = NamingUtils.toPascalCase(entity.getEntityName()) + dtoSuffix;
        String dtoPackage = config.getBasePackage() + ".dto";

        Set<String> imports = new HashSet<>();
        List<KotlinAttributeDtoModel> dtoAttributes = mapAttributesToDto(entity.getAttributes(), imports, isResponseDto, config);
        List<KotlinRelationshipDtoModel> dtoRelationships = mapRelationshipsToDto(
                config,
                entity,
                imports,
                isResponseDto,
                dtoPackage
        );

        return KotlinDtoModel.builder()
                .packageName(dtoPackage)
                .className(dtoClassName)
                .attributes(dtoAttributes)
                .relationships(dtoRelationships)
                .imports(imports)
                .build();
    }

    @Named("mapAttributesToDto")
    default List<KotlinAttributeDtoModel> mapAttributesToDto(List<AttributeDefinition> entityAttributes,
                                                             Set<String> imports,
                                                             boolean isResponseDto,
                                                             @Context ProjectConfiguration config) {
        if (entityAttributes == null) return new ArrayList<>();
        return entityAttributes.stream()
                .filter(attr -> {
                    if (attr.isPrimaryKey()) {
                        return isResponseDto;
                    }
                    boolean isAuditingEnabled = NamingUtils.checkProjectOption(config, ProjectOptions.ENABLE_JPA_AUDITING);
                    if (isAuditingEnabled && (attr.getAttributeName().equals("createdAt") || attr.getAttributeName().equals("updatedAt"))) {
                        return isResponseDto;
                    }
                    return true;
                })
                .map(attr -> {
                    String kotlinType = mapDbDataTypeToKotlinBaseType(attr.getDataType());
                    addPotentialKotlinTypeImport(imports, kotlinType);
                    return KotlinAttributeDtoModel.builder()
                            .name(NamingUtils.toCamelCase(attr.getAttributeName()))
                            .baseKotlinType(kotlinType)
                            .isNullable(!attr.isRequired() || attr.isPrimaryKey())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Named("mapRelationshipsToDto")
    default List<KotlinRelationshipDtoModel> mapRelationshipsToDto(
            ProjectConfiguration projectConfig,
            EntityDefinition currentEntity,
            Set<String> imports,
            boolean isResponseDto,
            String dtoPackageName) {
        if (!isResponseDto) return Collections.emptyList();
        List<KotlinRelationshipDtoModel> dtoRelationships = new ArrayList<>();

        Set<String> processedRelationshipNames = new HashSet<>();
        if (currentEntity.getRelationships() != null) {
            for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
                EntityDefinition targetEntityDef = relDef.getTargetEntity();
                if (targetEntityDef == null) continue;

                String relatedDtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
                String relatedDtoClassName = NamingUtils.toPascalCase(targetEntityDef.getEntityName()) + relatedDtoSuffix;
                String fieldName = NamingUtils.toCamelCase(relDef.getSourceFieldName());

                boolean isCollection = relDef.getRelationshipType() == RelationshipType.ONE_TO_MANY ||
                        relDef.getRelationshipType() == RelationshipType.MANY_TO_MANY;

                KotlinRelationshipDtoModel.KotlinRelationshipDtoModelBuilder builder = KotlinRelationshipDtoModel.builder()
                        .name(fieldName)
                        .relatedDtoClassName(relatedDtoClassName)
                        .isCollection(isCollection)
                        .collectionType(isCollection ? "Set" : null)
                        .isNullable(true);

                if (isCollection) {
                    builder.initializer("emptySet()");
                    builder.isNullable(false);
                    imports.add("kotlin.collections.Set");
                    imports.add("kotlin.collections.emptySet");
                }

                if (processedRelationshipNames.add(fieldName)) {
                    dtoRelationships.add(builder.build());
                }
            }
        }
        for (EntityDefinition otherEntity : projectConfig.getEntities()) {
            if (otherEntity.equals(currentEntity) || otherEntity.getRelationships() == null) continue;

            for (RelationshipDefinition relDefOnOther : otherEntity.getRelationships()) {
                if (relDefOnOther.getTargetEntity() != null &&
                        relDefOnOther.getTargetEntity().getEntityName().equals(currentEntity.getEntityName()) &&
                        relDefOnOther.getTargetFieldName() != null && !relDefOnOther.getTargetFieldName().isBlank()) {
                    String fieldNameOnCurrentEntity = NamingUtils.toCamelCase(relDefOnOther.getTargetFieldName());
                    String relatedDtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
                    String otherEntityDtoClassName = NamingUtils.toPascalCase(otherEntity.getEntityName()) + relatedDtoSuffix;

                    boolean isCollection;
                    switch (relDefOnOther.getRelationshipType()) {
                        case MANY_TO_ONE:
                        case MANY_TO_MANY:
                            isCollection = true;
                            break;
                        default:
                            isCollection = false;
                            break;
                    }

                    KotlinRelationshipDtoModel.KotlinRelationshipDtoModelBuilder builder = KotlinRelationshipDtoModel.builder()
                            .name(fieldNameOnCurrentEntity)
                            .relatedDtoClassName(otherEntityDtoClassName)
                            .isCollection(isCollection)
                            .collectionType(isCollection ? "Set" : null)
                            .isNullable(true);

                    if (isCollection) {
                        builder.initializer("emptySet()");
                        builder.isNullable(false);
                        imports.add("kotlin.collections.Set");
                        imports.add("kotlin.collections.emptySet");
                    }

                    if (processedRelationshipNames.add(fieldNameOnCurrentEntity)) {
                        dtoRelationships.add(builder.build());
                    }
                }
            }
        }
        return dtoRelationships;
    }


    default String mapDbDataTypeToKotlinBaseType(String definitionType) {
        if (definitionType == null) return "Any";
        return switch (definitionType.toLowerCase()) {
            case "string", "text", "varchar" -> "String";
            case "integer", "int" -> "Int";
            case "long", "bigint" -> "Long";
            case "double" -> "Double";
            case "float" -> "Float";
            case "decimal", "numeric" -> "java.math.BigDecimal";
            case "boolean", "bool" -> "Boolean";
            case "date" -> "java.time.LocalDate";
            case "timestamp", "datetime" -> "java.time.LocalDateTime";
            case "time" -> "java.time.LocalTime";
            case "uuid" -> "java.util.UUID";
            case "blob", "bytea" -> "ByteArray";
            default -> "String";
        };
    }

    default void addPotentialKotlinTypeImport(Set<String> imports, String baseKotlinType) {
        if (baseKotlinType.startsWith("java.time.") ||
            baseKotlinType.startsWith("java.math.") ||
            baseKotlinType.equals("java.util.UUID")) {
            imports.add(baseKotlinType);
        }
    }
}