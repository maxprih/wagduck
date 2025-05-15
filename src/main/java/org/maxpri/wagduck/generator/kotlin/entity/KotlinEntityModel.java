package org.maxpri.wagduck.generator.kotlin.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinEntityModel {
    private String packageName;
    private String className;
    private String tableName;
    private String description;

    private boolean includeAuditing;

    private Set<String> imports;
    private List<String> classAnnotations;

    private List<KotlinAttributeModel> attributes;
    private List<KotlinRelationshipModel> relationships;

    private KotlinAttributeModel createdAtAttribute;
    private KotlinAttributeModel updatedAtAttribute;
}