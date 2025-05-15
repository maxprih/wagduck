package org.maxpri.wagduck.generator.kotlin.entity;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component
public class KotlinEntityGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String ENTITY_TEMPLATE = "kotlin/entity.kt.ftl";
    private final KotlinEntityMapper kotlinEntityMapper;

    public KotlinEntityGenerator(FreeMarkerTemplateProcessor templateProcessor, KotlinEntityMapper kotlinEntityMapper) {
        super(templateProcessor);
        this.kotlinEntityMapper = kotlinEntityMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            KotlinEntityModel model = kotlinEntityMapper.toKotlinEntityModel(config, entity);
            String templateOutput = templateProcessor.process(ENTITY_TEMPLATE, model);
            String filename = NamingUtils.toPascalCase(entity.getEntityName()) + ".kt";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating Kotlin entity " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Kotlin entity file for " + entity.getEntityName(), e);
        }
    }
}