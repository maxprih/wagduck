package org.maxpri.wagduck.generator.kotlin.service;

import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class KotlinServiceGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String SERVICE_TEMPLATE = "kotlin/service.kt.ftl";
    private final KotlinServiceMapper metaMapper;

    public KotlinServiceGenerator(FreeMarkerTemplateProcessor templateProcessor, KotlinServiceMapper metaMapper) {
        super(templateProcessor);
        this.metaMapper = metaMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        boolean hasPrimaryKey = entity.getAttributes().stream().anyMatch(AttributeDefinition::isPrimaryKey);
        if (!hasPrimaryKey) {
            System.err.println("Skipping service generation for entity " + entity.getEntityName() + " as it has no primary key defined.");
            return null;
        }

        try {
            KotlinServiceModel model = metaMapper.toKotlinServiceDefModel(config, entity);
            String templateOutput = templateProcessor.process(SERVICE_TEMPLATE, model);
            String filename = model.getServiceClassName() + ".kt";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating Kotlin Service for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Kotlin Service for " + entity.getEntityName(), e);
        }
    }
}