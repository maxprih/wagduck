package org.maxpri.wagduck.generator.kotlin.exception;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KotlinControllerAdviceGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String CONTROLLER_ADVICE_TEMPLATE = "kotlin/controller_advice.kt.ftl";

    public KotlinControllerAdviceGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String packageName = config.getBasePackage() + ".exception";
            String dtoPackageName = config.getBasePackage() + ".dto";
            Map<String, Object> model = Map.of(
                    "packageName", packageName,
                    "dtoPackageName", dtoPackageName
            );
            String templateOutput = templateProcessor.process(CONTROLLER_ADVICE_TEMPLATE, model);
            String filename = "RestExceptionControllerAdvice.kt";

            String packagePath = packageName.replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Exception file for", e);
        }
    }
}
