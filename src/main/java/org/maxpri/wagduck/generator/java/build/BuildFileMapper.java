package org.maxpri.wagduck.generator.java.build;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.domain.enums.TargetLanguage; // Import TargetLanguage
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface BuildFileMapper {

    // --- Constants ---
    String OPT_USE_LOMBOK = "USE_LOMBOK";
    String OPT_INCLUDE_OPENAPI = "INCLUDE_OPENAPI";
    String OPT_INCLUDE_ACTUATOR = "INCLUDE_ACTUATOR";
    String OPT_INCLUDE_DEVTOOLS = "INCLUDE_DEVTOOLS";
    String OPT_INCLUDE_FLYWAY = "INCLUDE_FLYWAY";

    // --- Placeholder Versions (Replace with actual source: config, constants, etc.) ---
    String LOMBOK_VERSION = "1.18.30"; // Example: Use a recent version
    String MAPSTRUCT_VERSION = "1.5.5.Final"; // Example: Use a recent version
    String EVO_INFLECTOR_VERSION = "1.3"; // Example: Use a recent version
    // String SPRINGDOC_VERSION = "2.5.0"; // Example if needed and not managed by Spring Boot

    @Mapping(target = "groupId", source = "config", qualifiedByName = "deriveGroupId")
    @Mapping(target = "artifactId", expression = "java(config.getModuleName())")
    @Mapping(target = "version", expression = "java(\"0.0.1-SNAPSHOT\")")
    @Mapping(target = "projectName", expression = "java(config.getProjectName())")
    @Mapping(target = "basePackage", expression = "java(config.getBasePackage())")
    @Mapping(target = "javaVersion", expression = "java(config.getLanguageVersion())")
    @Mapping(target = "springBootVersion", expression = "java(\"3.4.5\")")
    @Mapping(target = "springBootStarters", source = "config", qualifiedByName = "determineStarters")
    @Mapping(target = "databaseDependencies", source = "config", qualifiedByName = "determineDatabaseDependencies")
    @Mapping(target = "utilityDependencies", source = "config", qualifiedByName = "determineUtilityDependencies")
    @Mapping(target = "testDependencies", source = "config", qualifiedByName = "determineTestDependencies")
    @Mapping(target = "plugins", source = "config", qualifiedByName = "determinePlugins")
    // Map language to useKotlin boolean for the template
    @Mapping(target = "useKotlin", expression = "java(config.getLanguage() == org.maxpri.wagduck.domain.enums.TargetLanguage.KOTLIN)")
        // Pass versions to the model if needed by the template (optional, depends on template design)
        // @Mapping(target = "mapstructVersion", expression = "java(MAPSTRUCT_VERSION)")
        // @Mapping(target = "evoInflectorVersion", expression = "java(EVO_INFLECTOR_VERSION)")
        // @Mapping(target = "lombokVersion", expression = "java(LOMBOK_VERSION)")
    BuildFileModel toBuildFileModel(ProjectConfiguration config);

    @Named("deriveGroupId")
    default String deriveGroupId(ProjectConfiguration config) {
        if (config.getBasePackage() != null) {
            String[] parts = config.getBasePackage().split("\\.");
            if (parts.length >= 2) {
                return parts[0] + "." + parts[1];
            } else if (parts.length == 1) {
                return parts[0];
            }
        }
        return "com.example"; // Fallback
    }

    @Named("determineStarters")
    default List<BuildFileModel.Dependency> determineStarters(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> starters = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();

        // Core starters (Scope defaults to implementation if not specified)
        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-web").build());
        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-data-jpa").build());
        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-validation").build());

        if (options.contains(OPT_INCLUDE_ACTUATOR)) {
            starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-actuator").build());
        }
        if (options.contains(OPT_INCLUDE_OPENAPI)) {
            // Version likely managed by spring boot dependency management or defined elsewhere
            starters.add(BuildFileModel.Dependency.builder().groupId("org.springdoc").artifactId("springdoc-openapi-starter-webmvc-ui").build());
        }
        // Example: Security starter
        // if (options.contains("INCLUDE_SECURITY")) {
        //     starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-security").build());
        // }
        return starters;
    }

    @Named("determineDatabaseDependencies")
    default List<BuildFileModel.Dependency> determineDatabaseDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> dbDeps = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();

        if (config.getDatabaseType() != null && config.getDatabaseType() != DatabaseType.NONE) {
            String runtimeScope = "runtimeOnly"; // Gradle/Maven use different names, but template handles 'runtimeOnly' -> runtime
            if (config.getBuildTool() == BuildTool.MAVEN) {
                runtimeScope = "runtime"; // Maven uses 'runtime'
            }

            switch (config.getDatabaseType()) {
                case POSTGRESQL:
                    dbDeps.add(BuildFileModel.Dependency.builder().groupId("org.postgresql").artifactId("postgresql").scope(runtimeScope).build());
                    break;
                case MYSQL:
                    dbDeps.add(BuildFileModel.Dependency.builder().groupId("com.mysql").artifactId("mysql-connector-j").scope(runtimeScope).build());
                    break;
                case H2:
                    dbDeps.add(BuildFileModel.Dependency.builder().groupId("com.h2database").artifactId("h2").scope(runtimeScope).build());
                    break;
                // Add other database types if needed
            }

            if (options.contains(OPT_INCLUDE_FLYWAY)) {
                dbDeps.add(BuildFileModel.Dependency.builder().groupId("org.flywaydb").artifactId("flyway-core").build());
                // Potentially add DB-specific Flyway dependency if required by Flyway version/DB
                // Example for PostgreSQL:
                // if (config.getDatabaseType() == DatabaseType.POSTGRESQL) {
                //     dbDeps.add(BuildFileModel.Dependency.builder().groupId("org.flywaydb").artifactId("flyway-database-postgresql").build());
                // }
            }
            // Add Liquibase similarly if needed
        }
        return dbDeps;
    }

    @Named("determineUtilityDependencies")
    default List<BuildFileModel.Dependency> determineUtilityDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> utils = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();
        boolean isGradle = config.getBuildTool() == BuildTool.GRADLE;
        boolean isKotlin = config.getLanguage() == TargetLanguage.KOTLIN;

        // --- Lombok ---
        if (options.contains(OPT_USE_LOMBOK)) {
            if (isGradle) {
                // Gradle: compileOnly for library, annotationProcessor/kapt for processor
                utils.add(BuildFileModel.Dependency.builder()
                        .groupId("org.projectlombok").artifactId("lombok")
                        .version(LOMBOK_VERSION) // Provide version if not managed
                        .scope("compileOnly")
                        .build());
                utils.add(BuildFileModel.Dependency.builder()
                        .groupId("org.projectlombok").artifactId("lombok")
                        .version(LOMBOK_VERSION) // Provide version if not managed
                        .scope(isKotlin ? "kapt" : "annotationProcessor")
                        .build());
            } else {
                // Maven: provided scope for library (processor handled by plugin)
                utils.add(BuildFileModel.Dependency.builder()
                        .groupId("org.projectlombok").artifactId("lombok")
                        .version(LOMBOK_VERSION) // Provide version if not managed
                        .scope("provided")
                        .build());
            }
        }

        // --- MapStruct --- (Assuming always included in this setup)
        // Core library dependency (implementation scope)
        utils.add(BuildFileModel.Dependency.builder()
                .groupId("org.mapstruct").artifactId("mapstruct")
                .version(MAPSTRUCT_VERSION) // Provide version if not managed
                .scope("implementation") // Use implementation scope
                .build());

        // Processor dependency
        if (isGradle) {
            // Gradle: annotationProcessor or kapt for the processor
            utils.add(BuildFileModel.Dependency.builder()
                    .groupId("org.mapstruct").artifactId("mapstruct-processor")
                    .version(MAPSTRUCT_VERSION) // Provide version if not managed
                    .scope(isKotlin ? "kapt" : "annotationProcessor")
                    .build());
        } else {
            // Maven: Processor is configured via maven-compiler-plugin, not a dependency scope.
            // No explicit processor dependency needed here for Maven, but ensure version is managed.
            // We might still add it with 'provided' scope if needed for IDEs, but it's optional.
            utils.add(BuildFileModel.Dependency.builder()
                    .groupId("org.mapstruct").artifactId("mapstruct-processor")
                    .version(MAPSTRUCT_VERSION) // Provide version if not managed
                    .scope("provided") // Optional for Maven, helps IDEs
                    .build());
        }


        // --- Evo Inflector --- (Assuming always needed for NamingUtils)
        utils.add(BuildFileModel.Dependency.builder()
                .groupId("org.atteo").artifactId("evo-inflector")
                .version(EVO_INFLECTOR_VERSION) // Provide version if not managed
                .scope("implementation") // Should be implementation scope
                .build());

        // --- Spring Boot DevTools ---
        if (options.contains(OPT_INCLUDE_DEVTOOLS)) {
            String runtimeScope = "runtimeOnly";
            if (!isGradle) {
                runtimeScope = "runtime"; // Maven uses 'runtime'
            }
            utils.add(BuildFileModel.Dependency.builder()
                    .groupId("org.springframework.boot").artifactId("spring-boot-devtools")
                    // Version managed by Spring Boot
                    .scope(runtimeScope)
                    .build());
        }

        return utils;
    }

    @Named("determineTestDependencies")
    default List<BuildFileModel.Dependency> determineTestDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> tests = new ArrayList<>();
        String testScope = "testImplementation"; // Gradle default
        if (config.getBuildTool() == BuildTool.MAVEN) {
            testScope = "test"; // Maven uses 'test'
        }

        tests.add(BuildFileModel.Dependency.builder()
                .groupId("org.springframework.boot").artifactId("spring-boot-starter-test")
                // Version managed by Spring Boot
                .scope(testScope)
                .build());

        // Add other test dependencies like Testcontainers based on options
        // if (options.contains("USE_TESTCONTAINERS")) {
        //     tests.add(BuildFileModel.Dependency.builder().groupId("org.testcontainers").artifactId("junit-jupiter").scope(testScope).build());
        //     // Add specific container modules, e.g.:
        //     if (config.getDatabaseType() == DatabaseType.POSTGRESQL) {
        //         tests.add(BuildFileModel.Dependency.builder().groupId("org.testcontainers").artifactId("postgresql").scope(testScope).build());
        //     }
        // }
        return tests;
    }

    @Named("determinePlugins")
    default Map<String, String> determinePlugins(ProjectConfiguration config) {
        Map<String, String> plugins = new HashMap<>();
        // This method seems primarily for Maven plugin configuration args.
        // Gradle plugins are added in the template's `plugins {}` block.
        // Gradle task configuration is done in the template's `tasks.withType<>` blocks.

        if (config.getBuildTool() == BuildTool.MAVEN) {
            // Configure annotation processors for Maven via compiler plugin args
            List<String> compilerArgs = new ArrayList<>();

            // Check if MapStruct is effectively included (assuming it always is here)
            compilerArgs.add("-Amapstruct.defaultComponentModel=spring");

            // Add Lombok config if used (though Lombok often works via agent/plugin)
            // if (config.getEnabledOptions() != null && config.getEnabledOptions().contains(OPT_USE_LOMBOK)) {
            // Lombok usually doesn't need compiler args like MapStruct does.
            // }

            if (!compilerArgs.isEmpty()) {
                plugins.put("maven-compiler-plugin-args", String.join(" ", compilerArgs));
            }

            // Add other Maven plugin configurations if needed
        }
        return plugins;
    }
}