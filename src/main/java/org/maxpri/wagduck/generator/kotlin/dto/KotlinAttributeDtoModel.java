package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KotlinAttributeDtoModel {
    private String name;            // e.g., username
    private String baseKotlinType;  // e.g., String, Int, java.time.LocalDateTime
    private boolean isNullable;     // Determines if '?' should be added
    private List<String> annotations;
}