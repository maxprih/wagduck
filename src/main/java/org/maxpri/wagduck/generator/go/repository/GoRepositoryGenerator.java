package org.maxpri.wagduck.generator.go.repository;

import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.BaseFileGenerator;
import org.maxpri.wagduck.generator.FreeMarkerTemplateProcessor;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.maxpri.wagduck.util.NamingUtils;
import org.springframework.stereotype.Component;

@Component("goRepositoryGenerator")
public class GoRepositoryGenerator extends BaseFileGenerator<EntityDefinition> {

    private static final String GO_REPO_INTERFACE_TEMPLATE = "go/repository_interface.go.ftl";
    private static final String GO_REPO_IMPLEMENTATION_TEMPLATE = "go/repository_implementation.go.ftl";
    private final GoRepositoryMapper goRepositoryMapper;

    public GoRepositoryGenerator(FreeMarkerTemplateProcessor templateProcessor, GoRepositoryMapper goRepositoryMapper) {
        super(templateProcessor);
        this.goRepositoryMapper = goRepositoryMapper;
    }

    public GeneratedFileResult generate(ProjectConfiguration config, EntityDefinition entity, boolean isInterface) {
        try {
            if (isInterface) {
                GoRepositoryInterfaceModel interfaceModel = goRepositoryMapper.mapToInterfaceModel(entity, config);
                interfaceModel.setModulePath(goRepositoryMapper.getGoModulePath(config));
                String output = templateProcessor.process(GO_REPO_INTERFACE_TEMPLATE, interfaceModel);

                String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + "_repository.go";
                String repositoryPackagePath = interfaceModel.getPackageName().replace('.', '/');
                String fullPath = repositoryPackagePath + "/" + goFileName;

                return new GeneratedFileResult(fullPath, output.getBytes());
            } else {
                GoRepositoryImplementationModel implModel = goRepositoryMapper.mapToImplementationModel(entity, config);
                implModel.setModulePath(goRepositoryMapper.getGoModulePath(config));
                String output = templateProcessor.process(GO_REPO_IMPLEMENTATION_TEMPLATE, implModel);

                String goFileName = NamingUtils.toSnakeCase(entity.getEntityName()) + "_repository_impl.go";
                String repositoryPackagePath = implModel.getPackageName().replace('.', '/');
                String fullPath = repositoryPackagePath + "/" + goFileName;

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