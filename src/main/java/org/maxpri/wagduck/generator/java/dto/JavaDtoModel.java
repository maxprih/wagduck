package org.maxpri.wagduck.generator.java.dto;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.generator.java.entity.JavaAttributeModel;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaDtoModel {
    private String packageName;
    private String className;
    private String description;
    private boolean useLombok = true;

    private Set<String> imports;
    private List<JavaAttributeModel> attributes;
}