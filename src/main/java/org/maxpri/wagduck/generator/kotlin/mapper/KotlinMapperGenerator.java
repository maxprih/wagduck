package org.maxpri.wagduck.generator.kotlin.mapper;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class KotlinMapperGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String MAPPER_TEMPLATE = "kotlin/mapper.kt.ftl";
    private final KotlinMapperMapper metaMapper;

    public KotlinMapperGenerator(FreeMarkerTemplateProcessor templateProcessor,
                                 KotlinMapperMapper metaMapper) {
        super(templateProcessor);
        this.metaMapper = metaMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            KotlinMapperModel model = metaMapper.toKotlinEntityDtoMapperDefModel(config, entity);
            String templateOutput = templateProcessor.process(MAPPER_TEMPLATE, model);
            String filename = model.getMapperName() + ".kt";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating Kotlin DTO-Entity Mapper for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace(); // Replace with proper logging
            throw new RuntimeException("Failed to generate Kotlin DTO-Entity Mapper for " + entity.getEntityName(), e);
        }
    }
}