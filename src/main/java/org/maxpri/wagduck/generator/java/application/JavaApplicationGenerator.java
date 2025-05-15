package org.maxpri.wagduck.generator.java.application;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaApplicationGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String APP_TEMPLATE = "java/application.java.ftl";
    private final JavaApplicationMapper applicationMapper;

    public JavaApplicationGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaApplicationMapper applicationMapper) {
        super(templateProcessor);
        this.applicationMapper = applicationMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            JavaApplicationModel model = applicationMapper.toJavaApplicationModel(config);
            String templateOutput = templateProcessor.process(APP_TEMPLATE, model);
            String filename = model.getClassName() + ".java";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating application class: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate application class", e);
        }
    }
}