package org.maxpri.wagduck.generator.java.exception;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JavaControllerAdviceGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private final String CONTROLLER_ADVICE_TEMPLATE = "java/controller.advice.java.ftl";

    public JavaControllerAdviceGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String packageName = config.getBasePackage() + ".exception";
            String dtoPackageName = config.getBasePackage() + ".dto";
            List<String> exceptionNames = config.getEntities().stream()
                    .map(entity -> entity.getEntityName() + "NotFoundException")
                    .toList();
            String packagePath = packageName.replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/RestExceptionControllerAdvice.java";
            Map<String, Object> model = Map.of(
                    "packageName", packageName,
                    "dtoPackageName", dtoPackageName,
                    "exceptions", exceptionNames
            );
            String content = templateProcessor.process(CONTROLLER_ADVICE_TEMPLATE, model);
            return new GeneratedFileResult(fullPath, content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate RestExceptionControllerAdvice.java file", e);
        }
    }
}
