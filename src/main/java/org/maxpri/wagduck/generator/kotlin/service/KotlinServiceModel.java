package org.maxpri.wagduck.generator.kotlin.service;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class KotlinServiceModel {
    private String packageName;              // e.g., org.maxpri.wagduck.service
    private Set<String> imports;             // For @Service, @Autowired, Entity, DTOs, Mapper, Repo, Exceptions

    private String serviceClassName;         // e.g., UserService
    private String entityClassName;          // e.g., User
    private String entityClassImport;        // e.g., org.maxpri.wagduck.domain.model.User
    private String requestDtoClassName;      // e.g., UserRequestDto
    private String requestDtoClassImport;    // e.g., org.maxpri.wagduck.dto.UserRequestDto
    private String responseDtoClassName;     // e.g., UserResponseDto
    private String responseDtoClassImport;   // e.g., org.maxpri.wagduck.dto.UserResponseDto
    private String mapperInterfaceName;      // e.g., UserMapper
    private String mapperFieldName;          // e.g., userMapper
    private String mapperClassImport;        // e.g., org.maxpri.wagduck.mapper.UserMapper
    private String repositoryInterfaceName;  // e.g., UserRepository
    private String repositoryFieldName;      // e.g., userRepository
    private String repositoryClassImport;    // e.g., org.maxpri.wagduck.repository.UserRepository

    private String primaryKeyType;           // Kotlin type of the PK, e.g., Long, java.util.UUID
    private String primaryKeyName;           // Name of the PK field in the entity, e.g., "id"

    private String entityNotFoundExceptionImport; // e.g., org.maxpri.wagduck.exception.EntityNotFoundException
    private String entityNotFoundExceptionName;   // e.g., EntityNotFoundException
}