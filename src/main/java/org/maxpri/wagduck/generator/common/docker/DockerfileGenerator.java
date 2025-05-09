package org.maxpri.wagduck.generator.common.docker;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class DockerfileGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String DOCKERFILE_TEMPLATE = "java/dockerfile.java.ftl";
    private final DockerfileMapper dockerfileMapper;

    public DockerfileGenerator(FreeMarkerTemplateProcessor templateProcessor, DockerfileMapper dockerfileMapper) {
        super(templateProcessor);
        this.dockerfileMapper = dockerfileMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            DockerfileModel model = dockerfileMapper.toDockerfileModel(config);
            String content = templateProcessor.process(DOCKERFILE_TEMPLATE, model);
            return new GeneratedFileResult("Dockerfile", content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Dockerfile", e);
        }
    }
}