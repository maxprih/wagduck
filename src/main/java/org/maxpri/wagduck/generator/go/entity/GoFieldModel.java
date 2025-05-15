package org.maxpri.wagduck.generator.go.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoFieldModel {
    private String name;
    private String type;
    private String gormTag;
    private String jsonTag;
    private String validationTag;
    private String description;
    private boolean isRelationship;
    private boolean isPointer;
    private boolean isSlice;
}