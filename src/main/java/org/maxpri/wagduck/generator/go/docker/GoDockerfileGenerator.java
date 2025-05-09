package org.maxpri.wagduck.generator.go.docker;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component("goDockerfileGenerator")
public class GoDockerfileGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String GO_DOCKERFILE_TEMPLATE = "go/dockerfile.go.ftl";
    private final GoDockerfileMapper goDockerfileMapper;

    public GoDockerfileGenerator(FreeMarkerTemplateProcessor templateProcessor, GoDockerfileMapper goDockerfileMapper) {
        super(templateProcessor);
        this.goDockerfileMapper = goDockerfileMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            GoDockerfileModel model = goDockerfileMapper.mapToDockerfileModel(config);
            String templateOutput = templateProcessor.process(GO_DOCKERFILE_TEMPLATE, model);

            String fileName = "Dockerfile";

            return new GeneratedFileResult(fileName, templateOutput.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go main file", e);
        }
    }
}