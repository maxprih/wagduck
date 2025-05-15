package org.maxpri.wagduck.generator.java.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaEntityModel {
    private String packageName;
    private String className;
    private String tableName;
    private String description;

    private boolean includeAuditing = true;
    private boolean useLombok = true;

    private Set<String> imports;
    private List<String> classAnnotations;

    private List<JavaAttributeModel> attributes;
    private List<JavaRelationshipModel> relationships;

    private JavaAttributeModel createdAtAttribute;
    private JavaAttributeModel updatedAtAttribute;
}
