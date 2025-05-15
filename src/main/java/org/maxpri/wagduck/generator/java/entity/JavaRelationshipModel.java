package org.maxpri.wagduck.generator.java.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JavaRelationshipModel {
    private String name;
    private String type;
    private String description;
    private List<String> annotations;
}