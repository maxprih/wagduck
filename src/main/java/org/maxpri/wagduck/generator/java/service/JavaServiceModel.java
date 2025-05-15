package org.maxpri.wagduck.generator.java.service;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JavaServiceModel {
    private String servicePackage;
    private String serviceClassName;
    private String entityClassName;
    private String repositoryClassName;
    private String requestDtoClassName;
    private String responseDtoClassName;
    private String entityMapperName;
    private String repositoryVariableName;
    private String entityMapperVariableName;
    private String primaryKeyType;
    private String primaryKeyName;
    private Set<String> imports;
    private String resourceNotFoundExceptionName;
    private Set<String> apiEndpoints;
}