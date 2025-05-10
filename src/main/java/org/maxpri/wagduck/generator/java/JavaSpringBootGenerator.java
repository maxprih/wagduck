package org.maxpri.wagduck.generator.java;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maxpri.wagduck.domain.entity.EntityDefinition;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.domain.enums.ProjectOptions;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.exception.NoEntitiesException;
import org.maxpri.wagduck.generator.LanguageGenerator;
import org.maxpri.wagduck.generator.common.GitignoreGenerator;
import org.maxpri.wagduck.generator.common.ReadmeGenerator;
import org.maxpri.wagduck.generator.common.compose.DockerComposeGenerator;
import org.maxpri.wagduck.generator.common.docker.DockerfileGenerator;
import org.maxpri.wagduck.generator.java.application.JavaApplicationGenerator;
import org.maxpri.wagduck.generator.common.build.BuildFileGenerator;
import org.maxpri.wagduck.generator.common.build.GradleSettingsGenerator;
import org.maxpri.wagduck.generator.common.config.AppConfigGenerator;
import org.maxpri.wagduck.generator.java.controller.JavaControllerGenerator;
import org.maxpri.wagduck.generator.java.dto.JavaDtoGenerator;
import org.maxpri.wagduck.generator.java.entity.JavaEntityGenerator;
import org.maxpri.wagduck.generator.java.exception.JavaControllerAdviceGenerator;
import org.maxpri.wagduck.generator.java.exception.JavaErrorResponseGenerator;
import org.maxpri.wagduck.generator.java.exception.JavaExceptionGenerator;
import org.maxpri.wagduck.generator.java.mapper.JavaMapperGenerator;
import org.maxpri.wagduck.generator.java.repository.JavaRepositoryGenerator;
import org.maxpri.wagduck.generator.java.service.JavaServiceGenerator;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class JavaSpringBootGenerator implements LanguageGenerator {

    private final JavaControllerGenerator javaControllerGenerator;
    private final JavaEntityGenerator javaEntityGenerator;
    private final JavaRepositoryGenerator javaRepositoryGenerator;
    private final JavaServiceGenerator javaServiceGenerator;
    private final JavaDtoGenerator javaDtoGenerator;
    private final JavaErrorResponseGenerator javaErrorResponseGenerator;
    private final JavaMapperGenerator javaMapperGenerator;
    private final JavaExceptionGenerator javaExceptionGenerator;
    private final JavaApplicationGenerator javaApplicationGenerator;
    private final JavaControllerAdviceGenerator javaControllerAdviceGenerator;
    private final BuildFileGenerator buildFileGenerator;
    private final GradleSettingsGenerator gradleSettingsGenerator;
    private final AppConfigGenerator appConfigGenerator;
    private final GitignoreGenerator gitignoreGenerator;
    private final ReadmeGenerator readmeGenerator;
    private final DockerfileGenerator dockerfileGenerator;
    private final DockerComposeGenerator dockerComposeGenerator;

    @Override
    public boolean supports(TargetLanguage language, TargetFramework framework) {
        return language == TargetLanguage.JAVA && framework == TargetFramework.SPRING_BOOT;
    }

    @Override
    public GeneratedFileResult generateProject(ProjectConfiguration config) {
        log.info("Starting full project generation for project: {}", config.getId());
        List<GeneratedFileResult> allFiles = new ArrayList<>();

        config.setEnabledOptions(Set.of(ProjectOptions.ENABLE_JPA_AUDITING.name(), "USE_LOMBOK", ProjectOptions.ENABLE_DOCKER.name()));
        try {
            allFiles.add(buildFileGenerator.generate(config, config));
            if (config.getBuildTool() == BuildTool.GRADLE) {
                allFiles.add(gradleSettingsGenerator.generate(config, config));
            }
            allFiles.add(javaApplicationGenerator.generate(config, config));
            allFiles.add(javaErrorResponseGenerator.generate(config, config));
            allFiles.add(appConfigGenerator.generate(config, config));
            allFiles.add(gitignoreGenerator.generate(config, config));
            allFiles.add(readmeGenerator.generate(config, config));
            allFiles.add(javaControllerAdviceGenerator.generate(config, config));

            if (config.getEnabledOptions().contains(ProjectOptions.ENABLE_DOCKER.name())) {
                allFiles.add(dockerfileGenerator.generate(config, config));
                allFiles.add(dockerComposeGenerator.generate(config, config));
            }
            List<EntityDefinition> entities = config.getEntities() != null ? config.getEntities() : Collections.emptyList();
            if (entities.isEmpty()) {
                throw new NoEntitiesException("No entities found for project: " + config.getId());
            } else {
                for (EntityDefinition entity : entities) {
                    allFiles.add(javaEntityGenerator.generate(config, entity));
                    allFiles.add(javaRepositoryGenerator.generate(config, entity));
                    allFiles.add(javaServiceGenerator.generate(config, entity));
                    allFiles.add(javaDtoGenerator.generate(config, entity, true));
                    allFiles.add(javaDtoGenerator.generate(config, entity, false));
                    allFiles.add(javaMapperGenerator.generate(config, entity));
                    allFiles.add(javaControllerGenerator.generate(config, entity));
                    allFiles.add(javaExceptionGenerator.generate(config, entity));
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