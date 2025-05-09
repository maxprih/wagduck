package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GoRepositoryMethodModel {
    private String name; // e.g., CreateUser, GetUserByID
    private String description; // e.g., "creates a new user in the database."
    private List<GoParameterModel> parameters; // e.g., (ctx context.Context, user *model.User)
    private List<GoParameterModel> returnTypes; // e.g., (*model.User, error)
    // For implementation template:
    private String bodyContent; // Pre-generated GORM logic snippet (optional, or built in template)
}