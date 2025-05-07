package org.maxpri.wagduck.generator.java.exception;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component
public class JavaExceptionGenerator extends BaseFileGenerator<EntityDefinition> {

    private final String EXCEPTION_TEMPLATE = "java/exception.java.ftl";
    private final JavaExceptionMapper javaExceptionMapper;

    public JavaExceptionGenerator(FreeMarkerTemplateProcessor templateProcessor, JavaExceptionMapper javaExceptionMapper) {
        super(templateProcessor);
        this.javaExceptionMapper = javaExceptionMapper;
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        JavaExceptionModel model = javaExceptionMapper.toJavaExceptionModel(config, entity);
        String templateOutput = templateProcessor.process(EXCEPTION_TEMPLATE, model);
        String filename = NamingUtils.toPascalCase(entity.getEntityName()) + "NotFoundException.java";
        String packagePath = model.getPackageName().replace('.', '/');
        String fullPath = "src/main/java/" + packagePath + "/" + filename;
        return new GeneratedFileResult(fullPath, templateOutput.getBytes());
    }
}
