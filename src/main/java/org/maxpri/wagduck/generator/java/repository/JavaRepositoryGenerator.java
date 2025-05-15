package org.maxpri.wagduck.generator.java.repository;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaRepositoryGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String REPOSITORY_TEMPLATE = "java/repository.java.ftl";
    private final JavaRepositoryMapper javaRepositoryMapper;

    public JavaRepositoryGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaRepositoryMapper javaRepositoryMapper) {
        super(templateProcessor);
        this.javaRepositoryMapper = javaRepositoryMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            JavaRepositoryModel model = javaRepositoryMapper.toJavaRepositoryModel(config, entity);
            String templateOutput = templateProcessor.process(REPOSITORY_TEMPLATE, model);
            String filename = model.getInterfaceName() + ".java";
            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating repository for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate repository file for " + entity.getEntityName(), e);
        }
    }
}