package org.maxpri.wagduck.service;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.dto.request.project.ProjectConfigurationCreateRequest;
import org.maxpri.wagduck.dto.response.project.ProjectConfigurationResponse;
import org.maxpri.wagduck.exception.ProjectNotFoundException;
import org.maxpri.wagduck.mapper.ProjectMapper;
import org.maxpri.wagduck.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public List<ProjectConfigurationResponse> findProjectsByOwnerId(UUID ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(projectMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public ProjectConfigurationResponse createProject(ProjectConfigurationCreateRequest request, UUID ownerId) {
        ProjectConfiguration projectConfiguration = projectMapper.requestToEntity(request);
        projectConfiguration.setOwnerId(ownerId);

        projectConfiguration.setLanguageVersion(request.getLanguage() == TargetLanguage.JAVA ? "21" : "1.24.3");
        projectConfiguration.setFrameworkVersion(request.getFramework() == TargetFramework.SPRING_BOOT ? "3.2.5" : "1.10.0");

        if (request.getLanguage() == TargetLanguage.JAVA || request.getLanguage() == TargetLanguage.KOTLIN) {
            String basePackage = request.getBasePackage();
            projectConfiguration.setBasePackage(basePackage);

            if (basePackage != null && !basePackage.isBlank()) {
                String[] parts = basePackage.split("\\.");
                String moduleName = parts[parts.length - 1];
                projectConfiguration.setModuleName(moduleName);
            } else {
                projectConfiguration.setModuleName(null);
            }
        } else if (request.getLanguage() == TargetLanguage.GO) {
            projectConfiguration.setBasePackage("");
            projectConfiguration.setModuleName(request.getModuleName());
        }

        return projectMapper.entityToResponse(projectRepository.save(projectConfiguration));
    }

    @Transactional
    public ProjectConfigurationResponse updateProject(UUID projectId, ProjectConfigurationCreateRequest request, UUID ownerId) {
        ProjectConfiguration projectConfiguration = findProjectByIdAndOwnerInternal(projectId, ownerId);
        projectMapper.updateEntityFromRequest(projectConfiguration, request);

        return projectMapper.entityToResponse(projectRepository.save(projectConfiguration));
    }

    public ProjectConfigurationResponse findProjectByIdAndOwner(UUID projectId, UUID ownerId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .map(projectMapper::entityToResponse)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }

    public ProjectConfiguration findProjectByIdAndOwnerInternal(UUID projectId, UUID ownerId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }
}
