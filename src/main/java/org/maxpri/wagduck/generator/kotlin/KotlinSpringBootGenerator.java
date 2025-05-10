package org.maxpri.wagduck.generator.kotlin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.generator.LanguageGenerator;
import org.maxpri.wagduck.generator.common.build.BuildFileGenerator;
import org.maxpri.wagduck.generator.common.build.GradleSettingsGenerator;
import org.maxpri.wagduck.generator.common.compose.DockerComposeGenerator;
import org.maxpri.wagduck.generator.common.config.AppConfigGenerator;
import org.maxpri.wagduck.generator.common.docker.DockerfileGenerator;
import org.maxpri.wagduck.generator.kotlin.application.KotlinApplicationGenerator;
import org.maxpri.wagduck.generator.kotlin.controller.KotlinControllerGenerator;
import org.maxpri.wagduck.generator.kotlin.dto.KotlinDtoGenerator;
import org.maxpri.wagduck.generator.kotlin.entity.KotlinEntityGenerator;
import org.maxpri.wagduck.generator.kotlin.exception.KotlinControllerAdviceGenerator;
import org.maxpri.wagduck.generator.kotlin.exception.KotlinErrorResponseGenerator;
import org.maxpri.wagduck.generator.kotlin.exception.KotlinNotFoundExceptionGenerator;
import org.maxpri.wagduck.generator.kotlin.mapper.KotlinMapperGenerator;
import org.maxpri.wagduck.generator.kotlin.repository.KotlinRepositoryGenerator;
import org.maxpri.wagduck.generator.kotlin.service.KotlinServiceGenerator;
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
public class KotlinSpringBootGenerator implements LanguageGenerator {

    private final KotlinEntityGenerator entityGenerator;
    private final KotlinRepositoryGenerator repositoryGenerator;
    private final KotlinDtoGenerator dtoGenerator;
    private final KotlinMapperGenerator mapperGenerator;
    private final KotlinServiceGenerator serviceGenerator;
    private final KotlinControllerGenerator controllerGenerator;
    private final KotlinNotFoundExceptionGenerator exceptionGenerator;
    private final KotlinErrorResponseGenerator errorResponseGenerator;
    private final KotlinControllerAdviceGenerator controllerAdviceGenerator;
    private final KotlinApplicationGenerator applicationGenerator;
    private final BuildFileGenerator buildFileGenerator;
    private final GradleSettingsGenerator gradleSettingsGenerator;
    private final AppConfigGenerator appConfigGenerator;
    private final DockerfileGenerator dockerfileGenerator;
    private final DockerComposeGenerator dockerComposeGenerator;

    @Override
    public boolean supports(TargetLanguage language, TargetFramework framework) {
        return language == TargetLanguage.KOTLIN && framework == TargetFramework.SPRING_BOOT;
    }

    @Override
    public GeneratedFileResult generateProject(ProjectConfiguration config) {
        log.info("Starting full project generation for project: {}", config.getId());
        List<GeneratedFileResult> allFiles = new ArrayList<>();

        config.getEnabledOptions().add("ENABLE_JPA_AUDITING");
        try {
            allFiles.add(exceptionGenerator.generate(config, config));
            allFiles.add(errorResponseGenerator.generate(config, config));
            allFiles.add(controllerAdviceGenerator.generate(config, config));
            allFiles.add(applicationGenerator.generate(config, config));
            allFiles.add(gradleSettingsGenerator.generate(config, config));
            allFiles.add(buildFileGenerator.generate(config, config));
            allFiles.add(appConfigGenerator.generate(config, config));
            allFiles.add(dockerfileGenerator.generate(config, config));
            allFiles.add(dockerComposeGenerator.generate(config, config));

            List<EntityDefinition> entities = config.getEntities() != null ? config.getEntities() : Collections.emptyList();
            for (EntityDefinition entity : entities) {
                allFiles.add(entityGenerator.generate(config, entity));
                allFiles.add(repositoryGenerator.generate(config, entity));
                allFiles.add(dtoGenerator.generate(config, entity, false));
                allFiles.add(dtoGenerator.generate(config, entity, true));
                allFiles.add(mapperGenerator.generate(config, entity));
                allFiles.add(serviceGenerator.generate(config, entity));
                allFiles.add(controllerGenerator.generate(config, entity));
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
