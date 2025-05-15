package org.maxpri.wagduck.generator.java.application;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaApplicationModel {
    private String packageName;
    private String className;
    private Set<String> imports;
    private List<String> annotations;
}
