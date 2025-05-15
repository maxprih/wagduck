package org.maxpri.wagduck.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationService {

    private final ProjectService projectService;
    private final List<LanguageGenerator> languageGenerators;

    @Transactional(readOnly = true)
    public GeneratedFileResult generateProject(UUID projectId, UUID ownerId) {
        ProjectConfiguration projectConfig = projectService.findProjectByIdAndOwnerInternal(projectId, ownerId);

        LanguageGenerator generator = languageGenerators.stream()
                .filter(gen -> gen.supports(projectConfig.getLanguage(), projectConfig.getFramework()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No suitable LanguageGenerator found for language: {} and framework: {}",
                            projectConfig.getLanguage(), projectConfig.getFramework());
                    return new UnsupportedOperationException(
                            "Code generation not supported for: " + projectConfig.getLanguage() + " / " + projectConfig.getFramework());
                });

        try {
            return generator.generateProject(projectConfig);
        } catch (Exception e) {
            throw new RuntimeException("Generation failed: " + e.getMessage(), e);
        }
    }
}
