package org.maxpri.wagduck.generator.java.service;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

@Component
public class JavaServiceGenerator extends BaseFileGenerator<EntityDefinition> { // Renamed class

    private static final String SERVICE_TEMPLATE = "java/service.java.ftl"; // Updated template name
    private final JavaServiceMapper javaServiceMapper;

    public JavaServiceGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaServiceMapper javaServiceMapper) { // Updated constructor
        super(templateProcessor);
        this.javaServiceMapper = javaServiceMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        try {
            JavaServiceModel model = javaServiceMapper.toJavaServiceModel(config, entity);
            String templateOutput = templateProcessor.process(SERVICE_TEMPLATE, model); // Use updated template name
            String filename = model.getServiceClassName() + ".java"; // Use updated model field

            String packagePath = model.getServicePackage().replace('.', '/'); // Use updated model field
            String fullPath = "src/main/java/" + packagePath + "/" + filename;

            return new GeneratedFileResult(fullPath, templateOutput.getBytes());

        } catch (Exception e) {
            System.err.println("Error generating service for " + entity.getEntityName() + ": " + e.getMessage()); // Updated log message
            e.printStackTrace();
            throw new RuntimeException("Failed to generate service file for " + entity.getEntityName(), e); // Updated exception message
        }
    }
}