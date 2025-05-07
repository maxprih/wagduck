package org.maxpri.wagduck.generator.java.application;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class JavaApplicationModel {
    private String packageName; // Base package
    private String className;   // e.g., MyProjectApplication
    private Set<String> imports; // Just SpringBootApplication for basic setup
    private List<String> annotations; // Just @SpringBootApplication
}
