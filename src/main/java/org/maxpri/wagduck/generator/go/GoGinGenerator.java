package org.maxpri.wagduck.generator.go;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.exception.NoEntitiesException;
import org.maxpri.wagduck.generator.LanguageGenerator;
import org.maxpri.wagduck.generator.common.compose.DockerComposeGenerator;
import org.maxpri.wagduck.generator.go.config.GoDbConfigGenerator;
import org.maxpri.wagduck.generator.go.docker.GoDockerfileGenerator;
import org.maxpri.wagduck.generator.go.entity.GoEntityGenerator;
import org.maxpri.wagduck.generator.go.handler.GoGinHandlerGenerator;
import org.maxpri.wagduck.generator.go.main.GoMainFileGenerator;
import org.maxpri.wagduck.generator.go.repository.GoRepositoryGenerator;
import org.maxpri.wagduck.generator.go.service.GoServiceGenerator;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoGinGenerator implements LanguageGenerator {

    private final GoEntityGenerator entityGenerator;
    private final GoRepositoryGenerator repositoryGenerator;
    private final GoServiceGenerator serviceGenerator;
    private final GoGinHandlerGenerator handlerGenerator;
    private final GoDbConfigGenerator configGenerator;
    private final GoMainFileGenerator mainFileGenerator;
    private final GoDockerfileGenerator dockerfileGenerator;
    private final DockerComposeGenerator dockerComposeGenerator;

    @Override
    public boolean supports(TargetLanguage language, TargetFramework framework) {
        return language == TargetLanguage.GO && framework == TargetFramework.GIN;
    }

    @Override
    public GeneratedFileResult generateProject(ProjectConfiguration config) {
        log.info("Starting full project generation for project: {}", config.getId());
        List<GeneratedFileResult> allFiles = new ArrayList<>();

        try {
            allFiles.add(configGenerator.generate(config, config));
            allFiles.add(mainFileGenerator.generate(config, config));
            allFiles.add(dockerfileGenerator.generate(config, config));
            allFiles.add(dockerComposeGenerator.generate(config, config));

            List<EntityDefinition> entities = config.getEntities() != null ? config.getEntities() : Collections.emptyList();
            if (entities.isEmpty()) {
                throw new NoEntitiesException("No entities found for project: " + config.getId());
            } else {
                for (EntityDefinition entity : entities) {
                    allFiles.add(entityGenerator.generate(config, entity));
                    allFiles.add(repositoryGenerator.generate(config, entity, false));
                    allFiles.add(repositoryGenerator.generate(config, entity, true));
                    allFiles.add(serviceGenerator.generate(config, entity, false));
                    allFiles.add(serviceGenerator.generate(config, entity, true));
                    allFiles.add(handlerGenerator.generate(config, entity));
                }
            }
            String zipFileName = (config.getModuleName() != null ? config.getModuleName() : config.getProjectName()) + ".zip";
            byte[] zipBytes;

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ZipOutputStream zos = new ZipOutputStream(baos)) {

                for (GeneratedFileResult fileResult : allFiles) {
                    ZipEntry entry = new ZipEntry(fileResult.filename());
                    zos.putNextEntry(entry);
                    zos.write(fileResult.contentBytes());
                    zos.closeEntry();
                }
                zos.finish();
                zipBytes = baos.toByteArray();
            }

            return new GeneratedFileResult(zipFileName, zipBytes);

        } catch (Exception e) {
            throw new RuntimeException("Full project generation failed: " + e.getMessage(), e);
        }
    }
}