package org.maxpri.wagduck.generator.go.config;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goDbConfigGenerator")
public class GoDbConfigGenerator extends BaseFileGenerator<ProjectConfiguration> {

    private static final String GO_DB_CONFIG_TEMPLATE = "go/db_config.go.ftl";
    private final GoDbConfigMapper goDbConfigMapper;

    public GoDbConfigGenerator(FreeMarkerTemplateProcessor templateProcessor, GoDbConfigMapper goDbConfigMapper) {
        super(templateProcessor);
        this.goDbConfigMapper = goDbConfigMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, ProjectConfiguration input) {
        try {
            GoDbConfigFileModel model = goDbConfigMapper.mapToDbConfigFileModel(config);
            String templateOutput = templateProcessor.process(GO_DB_CONFIG_TEMPLATE, model);

            String goFileName = NamingUtils.toSnakeCase("database.go");
            String configPackagePath = model.getPackageName().replace('.', '/');
            String fullPath = configPackagePath + "/" + goFileName;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go database config file", e);
        }
    }
}