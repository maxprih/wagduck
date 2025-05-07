package org.maxpri.wagduck.generator.java.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JavaExceptionModel {
    private String packageName;
    private String exceptionName;
}
