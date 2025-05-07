package org.maxpri.wagduck.generator.java.mapper;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaMapperModel {
    private String packageName;
    private String interfaceName;
    private String componentModel = "spring";
    private String entityClassName;
    private String entityPackage;
    private String requestDtoClassName;
    private String responseDtoClassName;
    private String dtoPackage;
    private String primaryKeyType;
    private Set<String> imports;
    private List<String> toDtoMappings;
    private List<String> toEntityMappings;
    private List<String> updateMappings;
}