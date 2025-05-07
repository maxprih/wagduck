package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import org.maxpri.wagduck.dto.request.project.ProjectConfigurationCreateRequest;
import org.maxpri.wagduck.dto.response.project.ProjectConfigurationResponse;
import org.maxpri.wagduck.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectConfigurationResponse> getProjectForUser(@PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findProjectByIdAndOwner(projectId, UUID.fromString(jwt.getSubject())));
    }

    @GetMapping
    public ResponseEntity<List<ProjectConfigurationResponse>> getAllProjectsForUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findProjectsByOwnerId(UUID.fromString(jwt.getSubject())));
    }

    @PostMapping
    public ResponseEntity<ProjectConfigurationResponse> createProject(
            @RequestBody ProjectConfigurationCreateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(request, UUID.fromString(jwt.getSubject())));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectConfigurationResponse> updateProject(
            @PathVariable UUID projectId,
            @RequestBody ProjectConfigurationCreateRequest request,
            @RequestParam UUID userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.updateProject(projectId, request, userId));
    }
}
