package org.maxpri.wagduck.generator.java.exception;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JavaErrorResponseGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private final String ERROR_RESPONSE_TEMPLATE = "java/error.response.java.ftl";

    public JavaErrorResponseGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String packageName = config.getBasePackage() + ".dto";
            String packagePath = packageName.replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/ErrorResponse.java";
            Map<String, Object> model = Map.of("packageName", packageName);
            String content = templateProcessor.process(ERROR_RESPONSE_TEMPLATE, model);
            return new GeneratedFileResult(fullPath, content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate ErrorResponse.java file", e);
        }
    }
}
