<#--
  Helper function: check if a list contains a dependency with a given artifactId
-->
<#function containsArtifactId depList artifactId>
  <#list depList as dep>
    <#if dep.artifactId == artifactId>
      <#return true>
    </#if>
  </#list>
  <#return false>
</#function>
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>${springBootVersion}</version>
        <relativePath/>
    </parent>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
    <name>${projectName}</name>
    <description>${projectDescription!"Generated Project"}</description>

    <properties>
        <java.version>${javaVersion}</java.version>
        <org.mapstruct.version>1.5.5.Final</org.mapstruct.version>
        <org.atteo.evoInflector.version>1.3</org.atteo.evoInflector.version>
        <lombok.version>1.18.30</lombok.version>
        <#-- Only define springdoc property if the starter is present -->
        <#if containsArtifactId(springBootStarters, "springdoc-openapi-starter-webmvc-ui")>
        <springdoc-openapi-ui.version>2.5.0</springdoc-openapi-ui.version>
        </#if>
    </properties>

    <dependencies>
        <#-- Spring Boot Starters -->
        <#list springBootStarters as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#if dep.version??>
            <version>${dep.version}</version>
            </#if>
            <#if dep.scope??>
            <scope>${dep.scope}</scope>
            </#if>
        </dependency>
        </#list>

        <#-- Database Dependencies -->
        <#list databaseDependencies as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#if dep.version??>
            <version>${dep.version}</version>
            </#if>
            <#if dep.scope??>
            <scope>${dep.scope}</scope>
            </#if>
        </dependency>
        </#list>

        <#-- Utility Dependencies -->
        <#list utilityDependencies as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#-- Use property for version if applicable -->
            <#if dep.groupId == "org.projectlombok">
            <version>${r"${lombok.version}"}</version>
            <#elseif dep.groupId == "org.mapstruct">
            <version>${r"${org.mapstruct.version}"}</version>
            <#elseif dep.groupId == "org.atteo" && dep.artifactId == "evo-inflector">
            <version>${r"${org.atteo.evoInflector.version}"}</version>
            <#elseif dep.version??>
            <version>${dep.version}</version>
            </#if>
            <#if dep.scope??>
            <scope>${dep.scope}</scope>
            </#if>
        </dependency>
        </#list>

        <#-- Test Dependencies -->
        <#list testDependencies as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#if dep.version??>
            <version>${dep.version}</version>
            </#if>
            <#if dep.scope??>
            <scope>${dep.scope}</scope>
            </#if>
        </dependency>
        </#list>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <annotationProcessorPaths>
                        <#-- Lombok processor -->
                        <#list utilityDependencies as dep>
                          <#if dep.groupId == "org.projectlombok">
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${r"${lombok.version}"}</version>
                        </path>
                          </#if>
                          <#if dep.groupId == "org.mapstruct" && dep.artifactId == "mapstruct-processor">
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${r"${org.mapstruct.version}"}</version>
                        </path>
                          </#if>
                        </#list>
                    </annotationProcessorPaths>
                    <compilerArgs>
                        <#if plugins["maven-compiler-plugin-args"]??>
                        <arg>${plugins["maven-compiler-plugin-args"]}</arg>
                        </#if>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>