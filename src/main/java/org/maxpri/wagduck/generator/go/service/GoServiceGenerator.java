package org.maxpri.wagduck.generator.go.service;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goServiceGenerator")
public class GoServiceGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String GO_SERVICE_INTERFACE_TEMPLATE = "go/service_interface.go.ftl";
    private static final String GO_SERVICE_IMPLEMENTATION_TEMPLATE = "go/service_implementation.go.ftl";
    private final GoServiceMapper goServiceMapper;

    public GoServiceGenerator(FreeMarkerTemplateProcessor templateProcessor, GoServiceMapper goServiceMapper) {
        super(templateProcessor);
        this.goServiceMapper = goServiceMapper;
    }

    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity, boolean isInterface) {
        try {
            if (isInterface) {
                GoServiceInterfaceModel interfaceModel = goServiceMapper.mapToInterfaceModel(entity, config);
                interfaceModel.setModulePath(config.getModuleName());
                String output = templateProcessor.process(GO_SERVICE_INTERFACE_TEMPLATE, interfaceModel);

                String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + "_service.go";
                String servicePackagePath = interfaceModel.getPackageName().replace('.', '/');
                String fullPath = servicePackagePath + "/" + goFileName;

                return new GeneratedFileResult(fullPath, output.getBytes());
            } else {
                GoServiceImplementationModel implModel = goServiceMapper.mapToImplementationModel(entity, config);
                implModel.setModulePath(config.getModuleName());
                String output = templateProcessor.process(GO_SERVICE_IMPLEMENTATION_TEMPLATE, implModel);

                String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + "_service_impl.go";
                String servicePackagePath = implModel.getPackageName().replace('.', '/');
                String fullPath = servicePackagePath + "/" + goFileName;

                return new GeneratedFileResult(fullPath, output.getBytes());
            }

        } catch (Exception e) {
            System.err.println("Error generating Go repository for " + entity.getEntityName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Go repository file for " + entity.getEntityName(), e);
        }
    }

    @Override
    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}