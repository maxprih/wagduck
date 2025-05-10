plugins {
    id("java") <#-- Keep for Java source compatibility & standard tasks, even if primary lang is Kotlin -->
    id("org.springframework.boot") version "${springBootVersion}"
    id("io.spring.dependency-management") version "1.1.5"

<#if useKotlin>
    kotlin("jvm") version "${kotlinVersion!"1.9.23"}" // Provide default or ensure kotlinVersion is in model
    kotlin("plugin.spring") version "${kotlinVersion!"1.9.23"}"
    kotlin("plugin.jpa") version "${kotlinVersion!"1.9.23"}" // For no-arg constructors for JPA entities
    id("org.jetbrains.kotlin.kapt") version "${kotlinVersion!"1.9.23"}" // Kapt for annotation processors
</#if>
}

group = "${groupId}"
version = "${version}"
<#-- Set Java compatibility. For pure Kotlin projects, this still influences bytecode target for libraries. -->
java.sourceCompatibility = JavaVersion.toVersion("${javaVersion}")

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    <#list springBootStarters as dep>
    implementation("${dep.groupId}:${dep.artifactId}")
    </#list>

    // Database Dependencies
    <#list databaseDependencies as dep>
    ${dep.scope!"implementation"}("${dep.groupId}:${dep.artifactId}<#if dep.version??>:${dep.version}</#if>")
    </#list>

    // Utility Dependencies
    <#list utilityDependencies as dep>
    ${dep.scope!"implementation"}("${dep.groupId}:${dep.artifactId}<#if dep.version??>:${dep.version}</#if>")
    </#list>

    // Test Dependencies
    <#list testDependencies as dep>
    ${dep.scope!"testImplementation"}("${dep.groupId}:${dep.artifactId}<#if dep.version??>:${dep.version}</#if>")
    </#list>

<#if useKotlin>
    // Kotlin Standard Library & Reflection (Spring Boot needs reflection)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Or kotlin-stdlib
    implementation("org.jetbrains.kotlin:kotlin-reflect")
<#else>
    // For Java-only projects, if mapstruct is used:
    // annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}") // Ensure mapstructVersion is in model
</#if>

<#-- Common annotation processors (like MapStruct) declared for both Java and Kotlin -->
<#-- If MapStruct is always used with Spring: -->
<#if mapstructVersion??> <#-- Assuming mapstructVersion is in your model -->
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    <#if useKotlin>
    kapt("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    <#else>
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    </#if>
</#if>

<#-- Add other annotation processors here similarly, e.g., Lombok for Java -->
<#if !useKotlin && lombokVersion??>
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
</#if>
}

// --- Task Configuration ---

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // Good practice
    // Configure annotation processor path for JavaCompile if needed (e.g., if some processors are Java-only or for mixed source)
    // Kapt typically handles this for Kotlin, but this ensures Java sources are also processed.
    options.annotationProcessorPath = configurations.getByName("annotationProcessor") // Standard name
    options.compilerArgs.addAll(listOf(
        "-Amapstruct.defaultComponentModel=spring" // Example for MapStruct, if still relevant for Java sources
        <#if javaCompilerArgs??>
            <#list javaCompilerArgs as arg>
        ,"${arg}"
            </#list>
        </#if>
    ))
}

<#if useKotlin>
kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        // Add other Kapt arguments here if needed for other annotation processors
        // e.g., arg("spring.configuration.output.dir", project.buildDir.absolutePath + "/generated/kaptKotlin")
    }
    // Optional: To make Kapt work with correct error types, especially with newer Gradle versions
    // correctErrorTypes = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict" // For better nullability annotation handling with Java libraries
            // "-Xjvm-default=all" // If you want all interface methods to be default methods (requires Kotlin 1.4+)
        )
        jvmTarget = "${javaVersion}" // Align Kotlin JVM target with Java version
        apiVersion = "${kotlinApiVersion!"1.9"}"     // Set Kotlin API version (e.g., "1.8", "1.9")
        languageVersion = "${kotlinLanguageVersion!"1.9"}" // Set Kotlin language version
    }
}
</#if>

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

<#-- Configure main class for Spring Boot executable JAR/WAR -->
springBoot {
    mainClass.set("${mainClassName}") // e.g., com.example.YourApplicationKt or com.example.YourApplication
}

<#--
// Optional: If you need to specify source sets explicitly (usually not needed with Kotlin plugin)
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
<#if useKotlin>
        kotlin {
            srcDirs("src/main/kotlin", "src/main/java") // Allow Kotlin to see Java and vice-versa if mixed
        }
        resources {
            srcDirs("src/main/resources")
        }
</#if>
    }
    test {
        java {
            srcDirs("src/test/java")
        }
<#if useKotlin>
        kotlin {
            srcDirs("src/test/kotlin", "src/test/java")
        }
        resources {
            srcDirs("src/test/resources")
        }
</#if>
    }
}
-->