package org.maxpri.wagduck.generator.go.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoFieldModel {
    private String name; // Go field name (e.g., UserName) - PascalCase
    private String type; // Go type (e.g., string, uint, time.Time, []OtherStruct, *OtherStruct)
    private String gormTag; // GORM struct tag (e.g., "column:user_name;unique;not null")
    private String jsonTag; // JSON struct tag (e.g., "userName,omitempty")
    private String validationTag; // Optional: for validation libraries like validator/v10
    private String description; // Comment for the field
    private boolean isRelationship; // To help template or mapper if needed
    private boolean isPointer; // For relationship types like *UserProfile
    private boolean isSlice; // For relationship types like []Order
}