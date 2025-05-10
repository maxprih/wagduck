package org.maxpri.wagduck.generator.common.build;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface BuildFileMapper {

    // --- Hardcoded Latest Stable Versions (as of knowledge cut-off, update as needed) ---
    String SPRING_BOOT_VERSION = "3.4.5"; // Update to a recent stable version
    String KOTLIN_VERSION = "1.9.23";     // Update to a recent stable version
    String KOTLIN_API_VERSION = "1.9";
    String KOTLIN_LANGUAGE_VERSION = "1.9";
    String MAPSTRUCT_VERSION = "1.5.5.Final";
    String LOMBOK_VERSION = "1.18.32"; // Update to a recent stable version
    String EVO_INFLECTOR_VERSION = "1.3";
    String SPRING_DOC_OPENAPI_VERSION = "2.5.0"; // Update to a recent stable version
    String DEPENDENCY_MANAGEMENT_VERSION = "1.1.5"; // For io.spring.dependency-management

    // --- Option Keys (from your original) ---
    String OPT_USE_LOMBOK = "USE_LOMBOK"; // Will only apply if !useKotlin
    String OPT_INCLUDE_OPENAPI = "INCLUDE_OPENAPI";
    String OPT_INCLUDE_ACTUATOR = "INCLUDE_ACTUATOR";
    String OPT_INCLUDE_DEVTOOLS = "INCLUDE_DEVTOOLS";
    String OPT_INCLUDE_FLYWAY = "INCLUDE_FLYWAY";


    @Mapping(target = "groupId", source = "config", qualifiedByName = "deriveGroupId")
    @Mapping(target = "artifactId", expression = "java(config.getModuleName())")
    @Mapping(target = "version", expression = "java(\"0.0.1-SNAPSHOT\")")
    @Mapping(target = "projectName", expression = "java(config.getProjectName())")
    @Mapping(target = "basePackage", expression = "java(config.getBasePackage())")
    @Mapping(target = "javaVersion", expression = "java(\"21\")")
    @Mapping(target = "springBootVersion", expression = "java(SPRING_BOOT_VERSION)")
    @Mapping(target = "useKotlin", expression = "java(config.getLanguage() == org.maxpri.wagduck.domain.enums.TargetLanguage.KOTLIN)")
    @Mapping(target = "kotlinVersion", expression = "java(config.getLanguage() == org.maxpri.wagduck.domain.enums.TargetLanguage.KOTLIN ? KOTLIN_VERSION : null)")
    @Mapping(target = "kotlinApiVersion", expression = "java(config.getLanguage() == org.maxpri.wagduck.domain.enums.TargetLanguage.KOTLIN ? KOTLIN_API_VERSION : null)")
    @Mapping(target = "kotlinLanguageVersion", expression = "java(config.getLanguage() == org.maxpri.wagduck.domain.enums.TargetLanguage.KOTLIN ? KOTLIN_LANGUAGE_VERSION : null)")
    @Mapping(target = "mainClassName", source = "config", qualifiedByName = "determineMainClassName")
    @Mapping(target = "springBootStarters", source = "config", qualifiedByName = "determineStarters")
    @Mapping(target = "databaseDependencies", source = "config", qualifiedByName = "determineDatabaseDependencies")
    @Mapping(target = "utilityDependencies", source = "config", qualifiedByName = "determineUtilityDependencies")
    @Mapping(target = "testDependencies", source = "config", qualifiedByName = "determineTestDependencies")
    @Mapping(target = "javaCompilerArgs", source = "config", qualifiedByName = "determineJavaCompilerArgs")
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
        return "com.example";
    }

    @Named("determineMainClassName")
    default String determineMainClassName(ProjectConfiguration config) {
        String appName = NamingUtils.toPascalCase(config.getModuleName()) + "Application";
        if (config.getLanguage() == TargetLanguage.KOTLIN) {
            return config.getBasePackage() + "." + appName + "Kt"; // Convention for top-level main in Kotlin
        }
        return config.getBasePackage() + "." + appName;
    }

    @Named("determineStarters")
    default List<BuildFileModel.Dependency> determineStarters(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> starters = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();
        String scope = "implementation";

        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-web").scope(scope).build());
        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-data-jpa").scope(scope).build());
        starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-validation").scope(scope).build());

        if (options.contains(OPT_INCLUDE_ACTUATOR)) {
            starters.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-actuator").scope(scope).build());
        }
        if (options.contains(OPT_INCLUDE_OPENAPI)) {
            starters.add(BuildFileModel.Dependency.builder().groupId("org.springdoc").artifactId("springdoc-openapi-starter-webmvc-ui").version(SPRING_DOC_OPENAPI_VERSION).scope(scope).build());
        }
        return starters;
    }

    @Named("determineDatabaseDependencies")
    default List<BuildFileModel.Dependency> determineDatabaseDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> dbDeps = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();
        String runtimeScope = "runtimeOnly";

        if (config.getDatabaseType() != null && config.getDatabaseType() != DatabaseType.NONE) {
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
            }
            if (options.contains(OPT_INCLUDE_FLYWAY)) {
                dbDeps.add(BuildFileModel.Dependency.builder().groupId("org.flywaydb").artifactId("flyway-core").scope("implementation").build());
                if (config.getDatabaseType() == DatabaseType.POSTGRESQL || config.getDatabaseType() == DatabaseType.MYSQL) { // Add DB specific Flyway driver
                    dbDeps.add(BuildFileModel.Dependency.builder().groupId("org.flywaydb").artifactId("flyway-database-" + config.getDatabaseType().name().toLowerCase()).scope("implementation").build());
                }
            }
        }
        return dbDeps;
    }

    @Named("determineUtilityDependencies")
    default List<BuildFileModel.Dependency> determineUtilityDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> utils = new ArrayList<>();
        Set<String> options = config.getEnabledOptions() != null ? config.getEnabledOptions() : Collections.emptySet();
        boolean isKotlin = config.getLanguage() == TargetLanguage.KOTLIN;

        // Lombok (only if Java and option enabled)
        if (!isKotlin && options.contains(OPT_USE_LOMBOK)) {
            utils.add(BuildFileModel.Dependency.builder().groupId("org.projectlombok").artifactId("lombok").version(LOMBOK_VERSION).scope("compileOnly").build());
            utils.add(BuildFileModel.Dependency.builder().groupId("org.projectlombok").artifactId("lombok").version(LOMBOK_VERSION).scope("annotationProcessor").build());
        }

        // MapStruct (always included)
        utils.add(BuildFileModel.Dependency.builder().groupId("org.mapstruct").artifactId("mapstruct").version(MAPSTRUCT_VERSION).scope("implementation").build());
        utils.add(BuildFileModel.Dependency.builder()
                .groupId("org.mapstruct").artifactId("mapstruct-processor").version(MAPSTRUCT_VERSION)
                .scope(isKotlin ? "kapt" : "annotationProcessor")
                .build());

        // Evo Inflector (assuming always needed)
        utils.add(BuildFileModel.Dependency.builder().groupId("org.atteo").artifactId("evo-inflector").version(EVO_INFLECTOR_VERSION).scope("implementation").build());

        // Spring Boot DevTools
        if (options.contains(OPT_INCLUDE_DEVTOOLS)) {
            utils.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-devtools").scope("runtimeOnly").build());
        }
        return utils;
    }

    @Named("determineTestDependencies")
    default List<BuildFileModel.Dependency> determineTestDependencies(ProjectConfiguration config) {
        List<BuildFileModel.Dependency> tests = new ArrayList<>();
        String testScope = "testImplementation";

        tests.add(BuildFileModel.Dependency.builder().groupId("org.springframework.boot").artifactId("spring-boot-starter-test").scope(testScope).build());
        if (config.getLanguage() == TargetLanguage.KOTLIN) {
            tests.add(BuildFileModel.Dependency.builder().groupId("org.jetbrains.kotlin").artifactId("kotlin-test-junit5").scope(testScope).build());
        }
        return tests;
    }

    @Named("determineJavaCompilerArgs")
    default List<String> determineJavaCompilerArgs(ProjectConfiguration config) {
        List<String> args = new ArrayList<>();
        // MapStruct compiler arg is generally always needed if MapStruct is used,
        // regardless of Java or Kotlin (Kapt handles Kotlin, but JavaCompile might still pick up Java sources).
        args.add("-Amapstruct.defaultComponentModel=spring");
        return args;
    }
}