package org.maxpri.wagduck.generator.common.compose;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface DockerComposeMapper {

    String DEFAULT_DB_SERVICE_NAME = "db";
    String DEFAULT_DB_USER = "user";
    String DEFAULT_DB_PASSWORD = "password";

    @Mapping(target = "appServiceName", source = "moduleName")
    @Mapping(target = "appPort", constant = "8080") // Default
    @Mapping(target = "databaseType", source = "databaseType")
    @Mapping(target = "dbServiceName", constant = DEFAULT_DB_SERVICE_NAME)
    @Mapping(target = "dbImage", source = "config", qualifiedByName = "determineDbImage")
    @Mapping(target = "dbVolumeName", expression = "java(deriveDbVolumeName(config))")
    @Mapping(target = "dbPort", source = "config", qualifiedByName = "determineDbPort")
    @Mapping(target = "dbName", source = "moduleName") // Use moduleName as default DB name
    @Mapping(target = "dbUser", constant = DEFAULT_DB_USER)
    @Mapping(target = "dbPassword", constant = DEFAULT_DB_PASSWORD)
    @Mapping(target = "dbEnvVars", source = "config", qualifiedByName = "determineDbEnvVars")
    @Mapping(target = "appDbUrlEnvVar", source = "config", qualifiedByName = "determineAppDbUrl")
    @Mapping(target = "appLanguage", expression = "java(config.getLanguage().name())")
    DockerComposeModel toDockerComposeModel(ProjectConfiguration config);

    @Named("determineDbImage")
    default String determineDbImage(ProjectConfiguration config) {
        return switch (config.getDatabaseType()) {
            case POSTGRESQL -> "postgres:15";
            case MYSQL -> "mysql:8";
            default -> null; // No image for H2/NONE
        };
    }

     @Named("deriveDbVolumeName")
     default String deriveDbVolumeName(ProjectConfiguration config) {
         String appName = config.getModuleName() != null ? config.getModuleName() : "app";
         return appName + "-db-data";
     }


    @Named("determineDbPort")
    default String determineDbPort(ProjectConfiguration config) {
        return switch (config.getDatabaseType()) {
            case POSTGRESQL -> "5432";
            case MYSQL -> "3306";
            default -> "0";
        };
    }

    @Named("determineDbEnvVars")
    default Map<String, String> determineDbEnvVars(ProjectConfiguration config) {
        Map<String, String> envVars = new HashMap<>();
        String dbName = config.getModuleName() != null ? config.getModuleName() : "appdb";

        switch (config.getDatabaseType()) {
            case POSTGRESQL:
                envVars.put("POSTGRES_DB", dbName);
                envVars.put("POSTGRES_USER", DEFAULT_DB_USER);
                envVars.put("POSTGRES_PASSWORD", DEFAULT_DB_PASSWORD);
                break;
            case MYSQL:
                envVars.put("MYSQL_DATABASE", dbName);
                envVars.put("MYSQL_USER", DEFAULT_DB_USER);
                envVars.put("MYSQL_PASSWORD", DEFAULT_DB_PASSWORD);
                envVars.put("MYSQL_ROOT_PASSWORD", DEFAULT_DB_PASSWORD);
                break;
            default:
                break; // No env vars needed for H2/NONE
        }
        return envVars;
    }

    @Named("determineAppDbUrl")
    default String determineAppDbUrl(ProjectConfiguration config) {
        String dbHost = DEFAULT_DB_SERVICE_NAME; // Use the service name
        String port = determineDbPort(config);
        String dbName = config.getModuleName() != null ? config.getModuleName() : "appdb";

        return switch (config.getDatabaseType()) {
            case POSTGRESQL -> String.format("jdbc:postgresql://%s:%s/%s", dbHost, port, dbName);
            case MYSQL -> String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", dbHost, port, dbName);
            case H2 -> "jdbc:h2:mem:" + dbName + "db";
            default -> ""; // No URL for NONE
        };
    }
}