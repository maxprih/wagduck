package org.maxpri.wagduck.generator.java.entity;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component
public class JavaEntityGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String ENTITY_TEMPLATE = "java/entity.java.ftl";
    private final JavaEntityMapper javaEntityMapper;

    public JavaEntityGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaEntityMapper javaEntityMapper) {
        super(templateProcessor);
        this.javaEntityMapper = javaEntityMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            config.getEntities();
            JavaEntityModel model = javaEntityMapper.toJavaEntityModel(config, entity);
            String templateOutput = templateProcessor.process(ENTITY_TEMPLATE, model);
            String filename = NamingUtils.toPascalCase(entity.getEntityName()) + ".java";
            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;
            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating entity " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate entity file for " + entity.getEntityName(), e);
        }
    }
}