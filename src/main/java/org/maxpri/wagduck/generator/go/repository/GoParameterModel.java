package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoParameterModel {
    private String name; // e.g., ctx, user, id
    private String type; // e.g., context.Context, *model.User, string, uint
}