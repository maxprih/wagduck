package org.maxpri.wagduck.generator.kotlin.exception;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KotlinNotFoundExceptionGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String EXCEPTION_TEMPLATE = "kotlin/entity_not_found.kt.ftl";

    public KotlinNotFoundExceptionGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String packageName = config.getBasePackage() + ".exception";
            Map<String, String> model = Map.of(
                    "packageName", packageName
            );
            String templateOutput = templateProcessor.process(EXCEPTION_TEMPLATE, model);
            String filename = "EntityNotFoundException.kt";

            String packagePath = packageName.replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Exception file for", e);
        }
    }
}
