package org.maxpri.wagduck.generator.kotlin.controller;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class KotlinControllerModel {
    private String packageName;
    private Set<String> imports;
    private String controllerClassName;
    private String baseRequestPath;
    private String serviceClassName;
    private String serviceFieldName;
    private String requestDtoClassName;
    private String responseDtoClassName;
    private String primaryKeyType;
    private String primaryKeyName;
}
