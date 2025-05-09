package org.maxpri.wagduck.generator.go.entity;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goEntityGenerator") // Give it a unique name if JavaEntityGenerator also exists
public class GoEntityGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String GO_ENTITY_TEMPLATE = "go/entity.go.ftl"; // Path to your .ftl file
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
            // String goModulePath = config.getOption("GO_MODULE_BASE_PATH"); // e.g. "github.com/user/project"
            // Path within the project, e.g., "internal/model/"
            String entityPackagePath = model.getPackageName().replace('.', '/'); // if package name can have dots

            // Assuming Go project structure where models are in a subfolder of the module root.
            // Example: if your Go module is "github.com/maxpri/mygoproject"
            // and entities are in package "model", they'd go into "model/user.go"
            // If package is "internal.model", they'd go into "internal/model/user.go"

            // For simplicity, let's assume the model.getPackageName() is like "model" or "internal/model"
            String relativePath = entityPackagePath + "/" + goFileName;


            return new GeneratedFileResult(relativePath, templateOutput.getBytes());

        } catch (Exception e) {
            // Log the error properly using your logging framework
            System.err.println("Error generating Go entity " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go entity file for " + entity.getEntityName(), e);
        }
    }
}