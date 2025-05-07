package org.maxpri.wagduck.generator;

import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;

public interface FileGenerator<InputModel> {
    GeneratedFileResult generate(ProjectConfiguration config, InputModel input);
}
