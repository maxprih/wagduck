package org.maxpri.wagduck.generator.kotlin.exception;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KotlinErrorResponseGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String ERROR_RESPONSE_TEMPLATE = "kotlin/error_response.kt.ftl";

    public KotlinErrorResponseGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String packageName = config.getBasePackage() + ".dto";
            Map<String, String> model = Map.of(
                    "packageName", packageName
            );
            String templateOutput = templateProcessor.process(ERROR_RESPONSE_TEMPLATE, model);

            String packagePath = packageName.replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/ErrorResponse.kt";

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate ErrorResponse.kt file", e);
        }
    }
}
