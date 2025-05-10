package org.maxpri.wagduck.generator.common.build;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GradleSettingsGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String SETTING_GRADLE_TEMPLATE = "common/settings.gradle.kts.ftl";

    public GradleSettingsGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            Map<String, Object> model = Map.of(
                "moduleName", config.getModuleName()
            );
            String content = templateProcessor.process(SETTING_GRADLE_TEMPLATE, model);
            return new GeneratedFileResult("settings.gradle.kts", content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate setting.gradle.kts file", e);
        }
    }
}