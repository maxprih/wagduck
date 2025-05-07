package org.maxpri.wagduck.generator.common.compose;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class DockerComposeGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String COMPOSE_TEMPLATE = "common/docker-compose.yml.ftl"; // Updated path
    private final DockerComposeMapper dockerComposeMapper;

    public DockerComposeGenerator(FreeMarkerTemplateProcessor templateProcessor, DockerComposeMapper dockerComposeMapper) {
        super(templateProcessor);
        this.dockerComposeMapper = dockerComposeMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        // Do not generate compose file if DB is H2 or NONE, as it wouldn't be very useful
        if (config.getDatabaseType() == DatabaseType.H2 || config.getDatabaseType() == DatabaseType.NONE) {
            System.out.println("Skipping docker-compose.yml generation for H2/NONE database type.");
            // Return null or an empty result to indicate no file was generated
            return null; // Or handle this appropriately in the calling code
        }

        try {
            DockerComposeModel model = dockerComposeMapper.toDockerComposeModel(config);
            String content = templateProcessor.process(COMPOSE_TEMPLATE, model);
            return new GeneratedFileResult("docker-compose.yml", content.getBytes()); // Place at root
        } catch (Exception e) {
            System.err.println("Error generating docker-compose.yml: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate docker-compose.yml", e);
        }
    }
}