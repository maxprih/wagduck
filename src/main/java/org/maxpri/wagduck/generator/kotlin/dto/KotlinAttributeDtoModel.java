package org.maxpri.wagduck.generator.kotlin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KotlinAttributeDtoModel {
    private String name;
    private String baseKotlinType;
    private boolean isNullable;
    private List<String> annotations;
}