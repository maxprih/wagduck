package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoRepositoryImplementationModel {
    private String packageName;
    private String structName;
    private String interfaceName;
    private String receiverName;
    private String entityName;
    private String entityPackageName;
    private String entityStructName;
    private String entityIdType;
    private String entityIdParameterName;
    private String entityIdStructField;
    private Set<String> imports;
    private List<GoRepositoryMethodModel> methods;
    private String description;
    private String modulePath;
}