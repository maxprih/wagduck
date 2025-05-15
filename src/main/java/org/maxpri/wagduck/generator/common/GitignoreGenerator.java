package org.maxpri.wagduck.generator.common;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GitignoreGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String GITIGNORE_TEMPLATE = "common/gitignore.ftl";

    public GitignoreGenerator(FreeMarkerTemplateProcessor templateProcessor) {
        super(templateProcessor);
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            String content = templateProcessor.process(GITIGNORE_TEMPLATE, Collections.emptyMap());
            return new GeneratedFileResult(".gitignore", content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate .gitignore file", e);
        }
    }
}