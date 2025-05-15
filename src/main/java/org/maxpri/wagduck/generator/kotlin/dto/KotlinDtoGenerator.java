package org.maxpri.wagduck.generator.kotlin.dto;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class KotlinDtoGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String DTO_TEMPLATE = "kotlin/dto.kt.ftl";
    private final KotlinDtoMapper kotlinDtoMapper;

    public KotlinDtoGenerator(FreeMarkerTemplateProcessor templateProcessor, KotlinDtoMapper kotlinDtoMapper) {
        super(templateProcessor);
        this.kotlinDtoMapper = kotlinDtoMapper;
    }

    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity, boolean isResponse) {
        try {
            KotlinDtoModel dtoModel = kotlinDtoMapper.buildDtoDefModel(config, entity, isResponse);
            String templateOutput = templateProcessor.process(DTO_TEMPLATE, dtoModel);
            String filename = dtoModel.getClassName() + ".kt";

            String packagePath = dtoModel.getPackageName().replace('.', '/');
            String fullPath = "src/main/kotlin/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        throw new UnsupportedOperationException("Use generateDtosForEntity to generate multiple DTO files.");
    }
}