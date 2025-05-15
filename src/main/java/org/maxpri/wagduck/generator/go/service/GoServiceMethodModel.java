package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.generator.go.repository.GoParameterModel;
import java.util.List;

@Data
@Builder
public class GoServiceMethodModel {
    private String name;
    private String description;
    private List<GoParameterModel> parameters;
    private List<GoParameterModel> returnTypes;
    private String correspondingRepositoryMethodName;
    @Builder.Default
    private List<UpdatableField> updatableFields = List.of();
}