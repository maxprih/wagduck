package org.maxpri.wagduck.generator.kotlin.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KotlinAttributeModel {
    private String name; // e.g., userId
    private String baseKotlinType; // e.g., Long, String, UUID, Int, BigDecimal, LocalDate (without '?' suffix)
    private boolean isNullable; // Determines if '?' should be added to the type
    private String description; // KDoc
    private boolean isPrimaryKey;
    private List<String> annotations; // e.g., "@Id", "@Column(name = "user_name")"
    private String initializer; // e.g., "null", "0L", "\"\"" (if needed for non-nullable or specific defaults)
}