package org.maxpri.wagduck.generator.go.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoEntityModel {
    private String packageName;
    private String structName;
    private String tableName;
    private String description;

    private boolean embedGormModel;
    private GoFieldModel idField;
    private GoFieldModel createdAtField;
    private GoFieldModel updatedAtField;

    private Set<String> imports;
    private List<GoFieldModel> attributes;
    private List<GoFieldModel> relationships;
}