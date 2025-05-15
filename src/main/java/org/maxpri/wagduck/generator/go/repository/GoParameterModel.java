package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoParameterModel {
    private String name;
    private String type;
}