package org.maxpri.wagduck.generator;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;

public interface LanguageGenerator {

    /**
     * Checks if this generator supports the given language and framework.
     * @param language Target language
     * @param framework Target framework
     * @return true if supported, false otherwise.
     */
    boolean supports(TargetLanguage language, TargetFramework framework);

    GeneratedFileResult generateProject(ProjectConfiguration config) throws Exception;

    // TODO: Add method for full project generation later
    // void generateFullProject(ProjectConfiguration config, Path outputDirectory) throws Exception;
}