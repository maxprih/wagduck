package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.generator.go.repository.GoParameterModel; // Re-using this for simplicity
import java.util.List;

@Data
@Builder
public class GoServiceMethodModel {
    private String name; // e.g., CreateUser, GetUserByID
    private String description; // e.g., "handles the business logic for creating a new user."
    private List<GoParameterModel> parameters; // e.g., (ctx context.Context, userRequest *dto.UserCreateRequest)
    private List<GoParameterModel> returnTypes; // e.g., (*model.User, error) or (*dto.UserResponse, error)
    private String correspondingRepositoryMethodName; // e.g., Create, GetByID (to call on the repo)
    @Builder.Default // Initialize with an empty list if not provided
    private List<UpdatableField> updatableFields = List.of(); // List of fields to be updated in the "Update" method
}