package org.maxpri.wagduck.generator.kotlin.repository;

import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class KotlinRepositoryGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String REPOSITORY_TEMPLATE = "kotlin/repository.kt.ftl";
    private final KotlinRepositoryMapper kotlinRepositoryMapper;

    public KotlinRepositoryGenerator(FreeMarkerTemplateProcessor templateProcessor, KotlinRepositoryMapper kotlinRepositoryMapper) {
        super(templateProcessor);
        this.kotlinRepositoryMapper = kotlinRepositoryMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        boolean hasPrimaryKey = entity.getAttributes().stream().anyMatch(AttributeDefinition::isPrimaryKey);
        if (!hasPrimaryKey) {
            System.err.println("Skipping repository generation for entity " + entity.getEntityName() + " as it has no primary key defined.");
            return null;
        }

        try {
            KotlinRepositoryModel model = kotlinRepositoryMapper.toKotlinRepositoryModel(config, entity);
            String templateOutput = templateProcessor.process(REPOSITORY_TEMPLATE, model);
            String filename = model.getRepositoryName() + ".kt";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Kotlin repository file for " + entity.getEntityName(), e);
        }
    }
}