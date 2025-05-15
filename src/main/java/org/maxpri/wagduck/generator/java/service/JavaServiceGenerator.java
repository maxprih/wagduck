package org.maxpri.wagduck.generator.java.service;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaServiceGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String SERVICE_TEMPLATE = "java/service.java.ftl";
    private final JavaServiceMapper javaServiceMapper;

    public JavaServiceGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaServiceMapper javaServiceMapper) {
        super(templateProcessor);
        this.javaServiceMapper = javaServiceMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            JavaServiceModel model = javaServiceMapper.toJavaServiceModel(config, entity);
            String templateOutput = templateProcessor.process(SERVICE_TEMPLATE, model);
            String filename = model.getServiceClassName() + ".java";

            String packagePath = model.getServicePackage().replace('.', '/');
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate service file for " + entity.getEntityName(), e);
        }
    }
}