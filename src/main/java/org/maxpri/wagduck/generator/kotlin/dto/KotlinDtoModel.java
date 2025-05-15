package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinDtoModel {
    private String packageName;
    private String className;
    private Set<String> imports;
    private List<KotlinAttributeDtoModel> attributes;
    private List<KotlinRelationshipDtoModel> relationships;
}