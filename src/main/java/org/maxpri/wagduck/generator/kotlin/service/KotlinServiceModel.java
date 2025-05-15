package org.maxpri.wagduck.generator.kotlin.service;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class KotlinServiceModel {
    private String packageName;
    private Set<String> imports;

    private String serviceClassName;
    private String entityClassName;
    private String entityClassImport;
    private String requestDtoClassName;
    private String requestDtoClassImport;
    private String responseDtoClassName;
    private String responseDtoClassImport;
    private String mapperInterfaceName;
    private String mapperFieldName;
    private String mapperClassImport;
    private String repositoryInterfaceName;
    private String repositoryFieldName;
    private String repositoryClassImport;

    private String primaryKeyType;
    private String primaryKeyName;

    private String entityNotFoundExceptionImport;
    private String entityNotFoundExceptionName;
}