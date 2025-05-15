package org.maxpri.wagduck.generator.common.compose;

import lombok.Builder;
import lombok.Data;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import java.util.Map;

@Data
@Builder
public class DockerComposeModel {
    private String appServiceName;
    private String appPort;
    private DatabaseType databaseType;
    private String dbServiceName;
    private String dbImage;
    private String dbVolumeName;
    private String dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private Map<String, String> dbEnvVars;
    private String appDbUrlEnvVar;
    private String appLanguage;
}
