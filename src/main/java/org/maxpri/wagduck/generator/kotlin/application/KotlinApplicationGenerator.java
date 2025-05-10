package org.maxpri.wagduck.generator.kotlin.application;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KotlinApplicationGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String APPLICATION_TEMPLATE = "kotlin/application.kt.ftl";

    public KotlinApplicationGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            Map<String, Object> model = Map.of(
                    "packageName", config.getBasePackage(),
                    "moduleName", NamingUtils.toPascalCase(config.getModuleName()),
                    "enableAuditing", config.getEnabledOptions().contains("ENABLE_JPA_AUDITING")
            );
            String templateOutput = templateProcessor.process(APPLICATION_TEMPLATE, model);
            String filename = NamingUtils.toPascalCase(config.getModuleName()) + "Application.kt";

            String packagePath = config.getBasePackage().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Exception file for", e);
        }
    }
}
