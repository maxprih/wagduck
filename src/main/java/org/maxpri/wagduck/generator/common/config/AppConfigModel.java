package org.maxpri.wagduck.generator.common.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppConfigModel {
    private String serverPort;

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriverClassName;
    private String jpaDdlAuto;
    private String jpaDatabasePlatform;
    private boolean jpaShowSql;

    private String applicationName;
    private String basePackage;
}