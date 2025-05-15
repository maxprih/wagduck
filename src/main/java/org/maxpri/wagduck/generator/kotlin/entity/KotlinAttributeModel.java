package org.maxpri.wagduck.generator.kotlin.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KotlinAttributeModel {
    private String name;
    private String baseKotlinType;
    private boolean isNullable;
    private String description;
    private boolean isPrimaryKey;
    private List<String> annotations;
    private String initializer;
}