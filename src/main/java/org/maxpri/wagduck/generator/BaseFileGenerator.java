package org.maxpri.wagduck.generator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseFileGenerator<InputModel> implements FileGenerator<InputModel> {
    protected final FreeMarkerTemplateProcessor templateProcessor;
}
