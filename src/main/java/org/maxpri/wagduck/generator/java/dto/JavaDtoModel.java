package org.maxpri.wagduck.generator.java.dto;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.generator.java.entity.JavaAttributeModel;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaDtoModel {
    private String packageName;         // e.g., org.maxpri.wagduck.dto
    private String className;           // e.g., UserRequest or UserResponse
    private String description;         // Class JavaDoc
    private boolean useLombok = true;   // Assume true

    private Set<String> imports;
    private List<JavaAttributeModel> attributes; // Reusing the attribute model

    // Specific flags if needed later (e.g., for validation groups)
}