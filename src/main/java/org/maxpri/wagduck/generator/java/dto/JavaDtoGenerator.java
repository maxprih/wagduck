package org.maxpri.wagduck.generator.java.dto;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaDtoGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String DTO_TEMPLATE = "java/dto.java.ftl";
    private final JavaDtoMapper javaDtoMapper;

    public JavaDtoGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaDtoMapper javaDtoMapper) {
        super(templateProcessor);
        this.javaDtoMapper = javaDtoMapper;
    }

    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity, boolean isRequest) {
        try {
            JavaDtoModel model;
            if (isRequest) {
                model = javaDtoMapper.toRequestDtoModel(config, entity);
            } else {
                model = javaDtoMapper.toResponseDtoModel(config, entity);
            }
            String templateOutput = templateProcessor.process(DTO_TEMPLATE, model);
            String filename = model.getClassName() + ".java";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating request DTO for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate request DTO file for " + entity.getEntityName(), e);
        }
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        throw new UnsupportedOperationException("Use generate(ProjectConfiguration config, EntityDefinition entity, boolean isRequest)");
    }
}