package org.maxpri.wagduck.generator.go.config;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GoDbConfigMapper {

    @Mapping(target = "packageName", expression = "java(deriveGoConfigPackage(config))")
    @Mapping(target = "configStructName", constant = "DatabaseConfig")
    @Mapping(target = "loadFunctionName", constant = "LoadDatabaseConfig")
    @Mapping(target = "dsnFunctionName", constant = "DSN")
    @Mapping(target = "initDbFunctionName", constant = "InitDatabaseConnection")
    @Mapping(target = "imports", source = "config", qualifiedByName = "collectDbConfigImports")
    @Mapping(target = "fields", source = "config", qualifiedByName = "generateDbConfigFields")
    GoDbConfigFileModel mapToDbConfigFileModel(ProjectConfiguration config);

    default String deriveGoConfigPackage(ProjectConfiguration config) {
        // return config.getGoConfigPackagePath(); // Ideal
        return "config"; // Default
    }

    @Named("collectDbConfigImports")
    default Set<String> collectDbConfigImports(ProjectConfiguration config) {
        Set<String> imports = new HashSet<>();
        imports.add("fmt");
        imports.add("os");
        imports.add("strconv");
        imports.add("log");
        imports.add("strings");

        imports.add("gorm.io/gorm");
        imports.add("gorm.io/driver/postgres");
        return imports;
    }

    @Named("generateDbConfigFields")
    default List<GoDbConfigFieldModel> generateDbConfigFields(ProjectConfiguration config) {
        List<GoDbConfigFieldModel> fields = new ArrayList<>();

        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("Host").fieldType("string").envVarName("DB_HOST").defaultValue(config.getModuleName()+"-db")
                .build());
        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("Port").fieldType("int").envVarName("DB_PORT").defaultValue("5432")
                .build());
        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("User").fieldType("string").envVarName("DB_USER").defaultValue("user")
                .build());
        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("Password").fieldType("string").envVarName("DB_PASSWORD").defaultValue("password").isSensitive(true)
                .build());
        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("DBName").fieldType("string").envVarName("DB_NAME").defaultValue(config.getModuleName())
                .build());
        fields.add(GoDbConfigFieldModel.builder()
                .fieldName("SSLMode").fieldType("string").envVarName("DB_SSLMODE").defaultValue("disable")
                .build());

        return fields;
    }
}