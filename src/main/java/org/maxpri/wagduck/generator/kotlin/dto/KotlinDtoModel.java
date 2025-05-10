package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class KotlinDtoModel {
    private String packageName;         // e.g., org.maxpri.wagduck.dto
    private String className;           // e.g., UserRequestDto, OrderResponseDto
    private Set<String> imports;        // All necessary imports for types and related DTOs
    private List<KotlinAttributeDtoModel> attributes;
    private List<KotlinRelationshipDtoModel> relationships;
    // No isResponseDto flag needed here, as each model instance is distinctly for request or response.
}