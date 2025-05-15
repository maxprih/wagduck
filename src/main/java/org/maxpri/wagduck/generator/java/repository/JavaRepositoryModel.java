package org.maxpri.wagduck.generator.java.repository;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class JavaRepositoryModel {
    private String packageName;
    private String interfaceName;
    private String entityClassName;
    private String entityPackage;
    private String primaryKeyType;
    private Set<String> imports;
}