package org.maxpri.wagduck.generator.kotlin.repository;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class KotlinRepositoryModel {
    private String packageName;
    private Set<String> imports;
    private String repositoryName;
    private String entityClassName;
    private String entityClassImport;
    private String primaryKeyType;
}