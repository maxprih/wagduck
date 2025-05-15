package org.maxpri.wagduck.generator.go.service;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class GoServiceImplementationModel {
    private String packageName;
    private String structName;
    private String interfaceName;
    private String receiverName;
    private String entityName;
    private String entityPackageName;
    private String entityStructName;
    private String entityIdType;
    private String repositoryInterfaceName;
    private String repositoryFieldName;
    private String repositoryPackageName;
    private Set<String> imports;
    private List<GoServiceMethodModel> methods;
    private String description;
    private String modulePath;
}