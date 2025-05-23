package org.maxpri.wagduck.generator.common.config;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Set;


@Component
public class AppConfigGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private final AppConfigMapper appConfigMapper;
    private static final String OPT_USE_YAML_CONFIG = "USE_YAML_CONFIG";

    public AppConfigGenerator(FreeMarkerTemplateProcessor templateProcessor, AppConfigMapper appConfigMapper) {
        super(templateProcessor);
        this.appConfigMapper = appConfigMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            AppConfigModel model = appConfigMapper.toAppConfigModel(config);
            String templateName;
            String filename;
            Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();
            if (options.contains(OPT_USE_YAML_CONFIG)) {
                templateName = "java/application.yml.ftl";
                filename = "application.yml";
            } else {
                templateName = "common/application.properties.ftl";
                filename = "application.properties";
            }
            String templateOutput = templateProcessor.process(templateName, model);

            String fullPath = "src/main/resources/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating application configuration file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate application configuration file", e);
        }
    }
}