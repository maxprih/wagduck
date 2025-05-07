package org.maxpri.wagduck.generator.java.mapper;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaMapperGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String MAPPER_TEMPLATE = "java/mapper.java.ftl";
    private final JavaMapperMapper mapperMapper;

    public JavaMapperGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaMapperMapper mapperMapper) {
        super(templateProcessor);
        this.mapperMapper = mapperMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            JavaMapperModel model = mapperMapper.toJavaMapperModel(config, entity);
            String templateOutput = templateProcessor.process(MAPPER_TEMPLATE, model);
            String filename = model.getInterfaceName() + ".java";

            String packagePath = model.getPackageName().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating entity DTO mapper for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate entity DTO mapper file for " + entity.getEntityName(), e);
        }
    }
}