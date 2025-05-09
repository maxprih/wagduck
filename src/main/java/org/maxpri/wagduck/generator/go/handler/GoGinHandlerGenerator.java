package org.maxpri.wagduck.generator.go.handler;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goGinHandlerGenerator")
public class GoGinHandlerGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String GO_GIN_HANDLER_TEMPLATE = "go/handler.go.ftl";
    private final GoGinHandlerMapper goGinHandlerMapper;

    public GoGinHandlerGenerator(FreeMarkerTemplateProcessor templateProcessor, GoGinHandlerMapper goGinHandlerMapper) {
        super(templateProcessor);
        this.goGinHandlerMapper = goGinHandlerMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            GoGinHandlerFileModel handlerFileModel = goGinHandlerMapper.mapToHandlerFileModel(entity, config);
            handlerFileModel.setModuleName(config.getModuleName());

            String output = templateProcessor.process(GO_GIN_HANDLER_TEMPLATE, handlerFileModel);

            String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + "_handler.go";
            String handlerPackagePath = handlerFileModel.getPackageName().replace('.', '/');
            String fullPath = handlerPackagePath + "/" + goFileName;

            return new GeneratedFileResult(fullPath, output.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating Go Gin handler for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go Gin handler file for " + entity.getEntityName(), e);
        }
    }
}