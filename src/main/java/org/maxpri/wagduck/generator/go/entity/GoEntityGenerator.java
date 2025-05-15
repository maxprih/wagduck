package org.maxpri.wagduck.generator.go.entity;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goEntityGenerator")
public class GoEntityGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String GO_ENTITY_TEMPLATE = "go/entity.go.ftl";
    private final GoEntityMapper goEntityMapper;

    public GoEntityGenerator(FreeMarkerTemplateProcessor templateProcessor, GoEntityMapper goEntityMapper) {
        super(templateProcessor);
        this.goEntityMapper = goEntityMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            GoEntityModel model = goEntityMapper.toGoEntityModel(config, entity);
            String templateOutput = templateProcessor.process(GO_ENTITY_TEMPLATE, model);

            String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + ".go";
            String entityPackagePath = model.getPackageName().replace('.', '/');
            String relativePath = entityPackagePath + "/" + goFileName;


            return new GeneratedFileResult(relativePath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating Go entity " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go entity file for " + entity.getEntityName(), e);
        }
    }
}