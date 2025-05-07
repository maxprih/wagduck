package org.maxpri.wagduck.generator.common;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReadmeGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String README_TEMPLATE = "common/readme.md.ftl";

    public ReadmeGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            Map<String, Object> model = Map.of(
                "projectName", config.getProjectName(),
                "buildTool", config.getBuildTool() != null ? config.getBuildTool().name().toLowerCase() : "maven"
            );
            String content = templateProcessor.process(README_TEMPLATE, model);
            return new GeneratedFileResult("README.md", content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate README.md file", e);
        }
    }
}