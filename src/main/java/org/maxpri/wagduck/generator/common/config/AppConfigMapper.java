package org.maxpri.wagduck.generator.common.config;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.DatabaseType;


@Mapper(componentModel = "spring")
public interface AppConfigMapper {
    @Mapping(target = "serverPort", constant = "8080")
    @Mapping(target = "dbUrl", source = "config", qualifiedByName = "determineDbUrl")
    @Mapping(target = "jpaDdlAuto", constant = "update")
    @Mapping(target = "jpaShowSql", constant = "false")
    @Mapping(target = "dbUsername", constant = "user")
    @Mapping(target = "dbPassword", constant = "password")
    @Mapping(target = "applicationName", source = "moduleName")
    @Mapping(target = "basePackage", source = "basePackage")
    @Mapping(target = "jpaDatabasePlatform", expression = "java(config.getDatabaseType().toString())")
    AppConfigModel toAppConfigModel(ProjectConfiguration config);

    @Named("determineDbUrl")
    default String determineDbUrl(ProjectConfiguration config) {
        if (config.getDatabaseType() == null || config.getDatabaseType() == DatabaseType.NONE) {
            return "";
        }
        return switch (config.getDatabaseType()) {
            case POSTGRESQL -> "jdbc:postgresql://localhost:5432/" + config.getModuleName();
            case MYSQL -> "jdbc:mysql://localhost:3306/" + config.getModuleName() + "?useSSL=false&serverTimezone=UTC";
            case H2 -> "jdbc:h2:mem:" + config.getModuleName()+"db";
            default -> "";
        };
    }
}