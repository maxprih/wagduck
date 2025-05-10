package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KotlinRelationshipDtoModel {
    private String name;                // e.g., profile, orders
    private String relatedDtoClassName; // e.g., ProfileResponseDto, OrderRequestDto
    private boolean isCollection;       // true if it's a Set or List
    private String collectionType;      // e.g., "Set", "List" (defaults to "Set")
    private boolean isNullable;         // For ToOne relationships, or if the collection itself can be null
    private String initializer;         // e.g. "emptySet()" for non-nullable collections
}