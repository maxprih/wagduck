package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KotlinRelationshipDtoModel {
    private String name;
    private String relatedDtoClassName;
    private boolean isCollection;
    private String collectionType;
    private boolean isNullable;
    private String initializer;
}