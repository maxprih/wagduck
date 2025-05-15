package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GoRepositoryMethodModel {
    private String name;
    private String description;
    private List<GoParameterModel> parameters;
    private List<GoParameterModel> returnTypes;
    private String bodyContent;
}