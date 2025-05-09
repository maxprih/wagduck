package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatableField {
    private String goFieldName; // PascalCase name of the field in the entity, e.g., "Population"
    private String goFieldType; // Go type of the field, e.g., "int64"
    // Future enhancement:
    // private boolean isPointerInDetailsDTO; // To handle partial updates with DTOs using pointers
    // private String detailsDTOFieldName; // If DTO field name differs from entity field name
}