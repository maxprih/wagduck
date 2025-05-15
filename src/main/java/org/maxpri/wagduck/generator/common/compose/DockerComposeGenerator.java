package org.maxpri.wagduck.generator.common.compose;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class DockerComposeGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String COMPOSE_TEMPLATE = "common/docker-compose.yml.ftl";
    private final DockerComposeMapper dockerComposeMapper;

    public DockerComposeGenerator(FreeMarkerTemplateProcessor templateProcessor, DockerComposeMapper dockerComposeMapper) {
        super(templateProcessor);
        this.dockerComposeMapper = dockerComposeMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        if (config.getDatabaseType() == DatabaseType.H2 || config.getDatabaseType() == DatabaseType.NONE) {
            System.out.println("Skipping docker-compose.yml generation for H2/NONE database type.");
            return null;
        }

        try {
            DockerComposeModel model = dockerComposeMapper.toDockerComposeModel(config);
            String content = templateProcessor.process(COMPOSE_TEMPLATE, model);
            return new GeneratedFileResult("docker-compose.yml", content.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating docker-compose.yml: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate docker-compose.yml", e);
        }
    }
}