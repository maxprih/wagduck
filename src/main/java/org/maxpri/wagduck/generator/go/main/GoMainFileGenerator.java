package org.maxpri.wagduck.generator.go.main;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component("goMainFileGenerator")
public class GoMainFileGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String GO_MAIN_FILE_TEMPLATE = "go/main.go.ftl";
    private final GoMainFileMapper goMainFileMapper;

    public GoMainFileGenerator(FreeMarkerTemplateProcessor templateProcessor, GoMainFileMapper goMainFileMapper) {
        super(templateProcessor);
        this.goMainFileMapper = goMainFileMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            GoMainFileModel model = goMainFileMapper.mapToMainFileModel(config);
            String templateOutput = templateProcessor.process(GO_MAIN_FILE_TEMPLATE, model);

            String fileName = "main.go";

            return new GeneratedFileResult(fileName, templateOutput.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go main file", e);
        }
    }
}