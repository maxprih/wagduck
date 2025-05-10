package org.maxpri.wagduck.generator.kotlin.mapper;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinMapperModel {
    private String packageName;              // e.g., org.maxpri.wagduck.mapper
    private Set<String> imports;             // For Mapper, Entity, DTOs, MappingTarget etc.
    private String mapperName;               // e.g., UserMapper
    private String entityClassName;          // e.g., User
    private String entityClassImport;        // e.g., org.maxpri.wagduck.domain.model.User
    private String requestDtoClassName;      // e.g., UserRequestDto
    private String requestDtoClassImport;    // e.g., org.maxpri.wagduck.dto.UserRequestDto
    private String responseDtoClassName;     // e.g., UserResponseDto
    private String responseDtoClassImport;   // e.g., org.maxpri.wagduck.dto.UserResponseDto
    private List<String> usesMapperNames;    // List of other mapper simple class names, e.g., ["OrderMapper", "ProfileMapper"]
}