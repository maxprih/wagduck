package org.maxpri.wagduck.generator.go.repository;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoRepositoryInterfaceModel {
    private String packageName;
    private String interfaceName;
    private String entityName;
    private String entityPackageName;
    private String entityStructName;
    private String entityIdType;
    private String entityIdParameterName;
    private Set<String> imports;
    private List<GoRepositoryMethodModel> methods;
    private String description;
    private String modulePath;
}