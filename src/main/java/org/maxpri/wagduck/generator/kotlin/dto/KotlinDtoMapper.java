package org.maxpri.wagduck.generator.kotlin.dto;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.entity.RelationshipDefinition;
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

    // Helper to build a single DTO model (either request or response)
    @Named("buildDtoDefModelInternal")
    default KotlinDtoModel buildDtoDefModel(ProjectConfiguration config,
                                            EntityDefinition entity,
                                            boolean isResponseDto) {
        String dtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
        String dtoClassName = NamingUtils.toPascalCase(entity.getEntityName()) + dtoSuffix;
        String dtoPackage = config.getBasePackage() + ".dto"; // Standard DTO package

        Set<String> imports = new HashSet<>();
        List<KotlinAttributeDtoModel> dtoAttributes = mapAttributesToDto(entity.getAttributes(), imports, isResponseDto, config);
        List<KotlinRelationshipDtoModel> dtoRelationships = mapRelationshipsToDto(
            config, // Pass ProjectConfiguration
            entity, // Pass current EntityDefinition
            imports,
            isResponseDto,
            dtoPackage // Pass dtoPackage to build correct import paths for related DTOs
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
                        return isResponseDto; // Include PK only in Response DTO
                    }
                    // Exclude audit fields from Request DTOs
                    boolean isAuditingEnabled = NamingUtils.checkProjectOption(config, org.maxpri.wagduck.domain.enums.ProjectOptions.ENABLE_JPA_AUDITING);
                    if (isAuditingEnabled && (attr.getAttributeName().equals("createdAt") || attr.getAttributeName().equals("updatedAt"))) {
                        return isResponseDto; // Include audit fields only in Response DTO
                    }
                    return true; // Include other fields
                })
                .map(attr -> {
                    String kotlinType = mapDbDataTypeToKotlinBaseType(attr.getDataType());
                    addPotentialKotlinTypeImport(imports, kotlinType);
                    return KotlinAttributeDtoModel.builder()
                            .name(NamingUtils.toCamelCase(attr.getAttributeName()))
                            .baseKotlinType(kotlinType)
                            .isNullable(!attr.isRequired() || attr.isPrimaryKey()) // PKs can be nullable in DTO before creation
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Named("mapRelationshipsToDto")
    default List<KotlinRelationshipDtoModel> mapRelationshipsToDto(
            ProjectConfiguration projectConfig, // For accessing all entities
            EntityDefinition currentEntity,     // The entity for which DTOs are being generated
            Set<String> imports,
            boolean isResponseDto,
            String dtoPackageName) { // e.g., "com.example.project.dto"
        if (!isResponseDto) return Collections.emptyList();
        List<KotlinRelationshipDtoModel> dtoRelationships = new ArrayList<>();

        // Combine outgoing and potentially incoming relationships similar to KotlinEntityMapper
        // For DTOs, we often mirror the entity structure.
        // Using the logic from KotlinEntityMapper.mapAllRelationshipsKt structure could be beneficial.

        // Simplified: Iterate through relationships defined on the current entity
        // and also consider relationships pointing TO this entity for a complete DTO picture.

        Set<String> processedRelationshipNames = new HashSet<>();

        // 1. Outgoing relationships (defined on currentEntity)
        if (currentEntity.getRelationships() != null) {
            for (RelationshipDefinition relDef : currentEntity.getRelationships()) {
                EntityDefinition targetEntityDef = relDef.getTargetEntity();
                if (targetEntityDef == null) continue; // Should not happen if data is consistent

                String relatedDtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
                String relatedDtoClassName = NamingUtils.toPascalCase(targetEntityDef.getEntityName()) + relatedDtoSuffix;
                String fieldName = NamingUtils.toCamelCase(relDef.getSourceFieldName());

                boolean isCollection = relDef.getRelationshipType() == RelationshipType.ONE_TO_MANY ||
                                       relDef.getRelationshipType() == RelationshipType.MANY_TO_MANY;

                KotlinRelationshipDtoModel.KotlinRelationshipDtoModelBuilder builder = KotlinRelationshipDtoModel.builder()
                        .name(fieldName)
                        .relatedDtoClassName(relatedDtoClassName)
                        .isCollection(isCollection)
                        .collectionType(isCollection ? "Set" : null) // Default to Set for collections
                        .isNullable(true); // Most relationships in DTOs are nullable, or initialized if collections

                if (isCollection) {
                    builder.initializer("emptySet()"); // Initialize collections
                    builder.isNullable(false); // Collections themselves are often non-null
                    imports.add("kotlin.collections.Set"); // Or MutableSet if modification is implied by requestDTO
                    imports.add("kotlin.collections.emptySet");
                }
                 // Import for the DTO type itself, if it's in the same DTO package, it's fine, otherwise qualified.
                // Assuming all DTOs are in the same dtoPackageName for simplicity.
                // If DTOs could be in different packages based on domain, this would need adjustment.
                // imports.add(dtoPackageName + "." + relatedDtoClassName); // Not strictly needed if same package

                if (processedRelationshipNames.add(fieldName)) {
                    dtoRelationships.add(builder.build());
                }
            }
        }

        // 2. Incoming relationships (where currentEntity is the target)
        // This makes DTOs more comprehensive, e.g. UserResponseDto showing its Orders.
        for (EntityDefinition otherEntity : projectConfig.getEntities()) {
            if (otherEntity.equals(currentEntity) || otherEntity.getRelationships() == null) continue;

            for (RelationshipDefinition relDefOnOther : otherEntity.getRelationships()) {
                // Check if the target of this relationship is the currentEntity
                // and if an inverse field name is specified (targetFieldName)
                if (relDefOnOther.getTargetEntity() != null &&
                    relDefOnOther.getTargetEntity().getEntityName().equals(currentEntity.getEntityName()) &&
                    relDefOnOther.getTargetFieldName() != null && !relDefOnOther.getTargetFieldName().isBlank()) {

                    // This 'otherEntity' has a field pointing to 'currentEntity'.
                    // 'currentEntity' will have an inverse field named 'relDefOnOther.getTargetFieldName()'.
                    String fieldNameOnCurrentEntity = NamingUtils.toCamelCase(relDefOnOther.getTargetFieldName());
                    String relatedDtoSuffix = isResponseDto ? "ResponseDto" : "RequestDto";
                    String otherEntityDtoClassName = NamingUtils.toPascalCase(otherEntity.getEntityName()) + relatedDtoSuffix;

                    boolean isCollection;
                    // Determine if this inverse relationship is a collection on currentEntity
                    // OneToMany on otherEntity -> ManyToOne on currentEntity (not a collection on current)
                    // ManyToOne on otherEntity -> OneToMany on currentEntity (is a collection on current)
                    // ManyToMany on otherEntity -> ManyToMany on currentEntity (is a collection on current)
                    // OneToOne on otherEntity -> OneToOne on currentEntity (not a collection on current)
                    switch (relDefOnOther.getRelationshipType()) {
                        case MANY_TO_ONE: // other has ManyToOne to current -> current has OneToMany from other
                        case MANY_TO_MANY: // other has ManyToMany with current -> current has ManyToMany with other
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
                    // imports.add(dtoPackageName + "." + otherEntityDtoClassName);

                    if (processedRelationshipNames.add(fieldNameOnCurrentEntity)) {
                        dtoRelationships.add(builder.build());
                    }
                }
            }
        }
        return dtoRelationships;
    }


    // --- Utility methods (can be refactored to a shared class with KotlinEntityMapper) ---
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