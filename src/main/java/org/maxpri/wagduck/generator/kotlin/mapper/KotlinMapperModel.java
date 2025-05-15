package org.maxpri.wagduck.generator.kotlin.mapper;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinMapperModel {
    private String packageName;
    private Set<String> imports;
    private String mapperName;
    private String entityClassName;
    private String entityClassImport;
    private String requestDtoClassName;
    private String requestDtoClassImport;
    private String responseDtoClassName;
    private String responseDtoClassImport;
    private List<String> usesMapperNames;
}