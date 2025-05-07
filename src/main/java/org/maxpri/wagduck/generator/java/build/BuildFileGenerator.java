package org.maxpri.wagduck.generator.java.build;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class BuildFileGenerator extends BaseFileGenerator<ProjectConfiguration> { // Input is ProjectConfiguration

    private final BuildFileMapper buildFileMapper;

    public BuildFileGenerator(FreeMarkerTemplateProcessor templateProcessor, BuildFileMapper buildFileMapper) {
        super(templateProcessor);
        this.buildFileMapper = buildFileMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) { // Input is config itself
        try {
//            config.setEnabledOptions(Set.of("USE_LOMBOK"));
            BuildFileModel model = buildFileMapper.toBuildFileModel(config);
            String templateName;
            String filename;

            if (config.getBuildTool() == BuildTool.GRADLE) {
                templateName = "java/build.gradle.kts.ftl";
                filename = "build.gradle.kts";
            } else { // Default to Maven
                templateName = "java/pom.xml.ftl";
                filename = "pom.xml";
            }

            String templateOutput = templateProcessor.process(templateName, model);

            // Build files go in the root directory
            return new GeneratedFileResult(filename, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating build file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate build file", e);
        }
    }
}