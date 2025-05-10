package org.maxpri.wagduck.generator.kotlin.controller;

import org.maxpri.wagduck.domain.entity.AttributeDefinition;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class KotlinControllerGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String CONTROLLER_TEMPLATE = "kotlin/controller.kt.ftl";
    private final KotlinControllerMapper metaMapper;

    public KotlinControllerGenerator(FreeMarkerTemplateProcessor templateProcessor, KotlinControllerMapper metaMapper) {
        super(templateProcessor);
        this.metaMapper = metaMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        boolean hasPrimaryKey = entity.getAttributes().stream().anyMatch(AttributeDefinition::isPrimaryKey);
        if (!hasPrimaryKey) {
            System.err.println("Skipping controller generation for entity " + entity.getEntityName() + " as it has no primary key defined.");
            return null;
        }
        try {
            KotlinControllerModel model = metaMapper.toKotlinControllerDefModel(config, entity);
            String templateOutput = templateProcessor.process(CONTROLLER_TEMPLATE, model);
            String filename = model.getControllerClassName() + ".kt";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            System.err.println("Error generating Kotlin Controller for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace(); // Replace with proper logging
            throw new RuntimeException("Failed to generate Kotlin Controller for " + entity.getEntityName(), e);
        }
    }
}