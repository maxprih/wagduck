package org.maxpri.wagduck.generator.java.service;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JavaServiceModel {
    // Packages
    private String basePackage;
    private String servicePackage;          // Renamed from serviceImplPackage
    private String repositoryPackage;
    private String dtoPackage;
    private String entityPackage;
    private String exceptionPackage;

    // Class Names
    private String serviceClassName;        // Renamed from serviceImplName
    private String entityClassName;
    private String repositoryClassName;
    private String requestDtoClassName;
    private String responseDtoClassName;
    private String entityMapperName;        // Convention for Entity <-> DTO Mapper

    // Variable Names
    private String repositoryVariableName;
    private String entityMapperVariableName; // Convention

    // Primary Key
    private String primaryKeyType;
    private String primaryKeyName;

    // Imports
    private Set<String> imports;            // Renamed from implImports

    // Exception Names
    private String resourceNotFoundExceptionName = "ResourceNotFoundException";
}