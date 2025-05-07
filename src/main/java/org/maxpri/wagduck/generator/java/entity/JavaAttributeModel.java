package org.maxpri.wagduck.generator.java.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JavaAttributeModel {
    private String name; // e.g., userId
    private String type; // e.g., Long, String, UUID, Integer, BigDecimal, LocalDate
    private String description; // JavaDoc
    private boolean isPrimaryKey;
    private List<String> annotations; // e.g., "@Id", "@Column(name = "user_name")", "@NotNull" (from jakarta.validation if needed)
    // We put full annotations here to give the template more control
}