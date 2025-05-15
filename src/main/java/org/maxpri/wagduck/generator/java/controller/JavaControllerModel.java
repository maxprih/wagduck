package org.maxpri.wagduck.generator.java.controller;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JavaControllerModel {
    private String controllerPackage;
    private String entityPackage;
    private String entityClassName;
    private String entityVariableName;
    private String serviceClassName;
    private String serviceVariableName;
    private String requestDtoClassName;
    private String responseDtoClassName;
    private String primaryKeyType;
    private String primaryKeyName;
    private String basePath;
    private Set<String> imports;
    private Set<String> apiEndpoints;
}