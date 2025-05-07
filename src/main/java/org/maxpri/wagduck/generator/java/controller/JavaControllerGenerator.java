package org.maxpri.wagduck.generator.java.controller;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component
public class JavaControllerGenerator extends BaseFileGenerator<EntityDefinition> {

    private final String CONTROLLER_TEMPLATE = "java/controller.java.ftl";
    private final JavaControllerMapper javaControllerMapper;

    public JavaControllerGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaControllerMapper javaControllerMapper) {
        super(templateProcessor);
        this.javaControllerMapper = javaControllerMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        JavaControllerModel model = javaControllerMapper.toJavaControllerModel(config, entity);
        String templateOutput = templateProcessor.process(CONTROLLER_TEMPLATE, model);
        String filename = NamingUtils.toPascalCase(entity.getEntityName()) + "Controller.java";
        String packagePath = model.getControllerPackage().replace('.', '/');
        String fullPath = "src/main/java/" + packagePath + "/" + filename;
        return new GeneratedFileResult(fullPath, templateOutput.getBytes());
    }
}
