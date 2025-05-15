package org.maxpri.wagduck.generator;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;

public interface LanguageGenerator {

    boolean supports(TargetLanguage language, TargetFramework framework);

    GeneratedFileResult generateProject(ProjectConfiguration config);
}