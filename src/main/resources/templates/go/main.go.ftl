package main

import (
    "log"
    "os"

    "github.com/gin-gonic/gin"

    "${moduleName}${configPackagePath}"
<#if autoMigrateEntities>
    "${moduleName}${modelsPackagePath}"
</#if>
    "${moduleName}${repositoryPackagePath}"
    "${moduleName}${servicePackagePath}"
    "${moduleName}${handlerPackagePath}"

<#if imports?has_content>
    <#list imports?sort as imp>
    "${imp}"
    </#list>
</#if>
)

func main() {
    appConfig := ${configPackagePath?keep_after_last("/")}.LoadDatabaseConfig()

    db, err := ${configPackagePath?keep_after_last("/")}.InitDatabaseConnection(appConfig)
    if err != nil {
        log.Fatalf("Failed to connect to database: %v", err)
    }

<#if autoMigrateEntities && entitiesToWire?has_content>
    err = db.AutoMigrate(
    <#list entitiesToWire as etw>
        &${modelsPackagePath?keep_after_last("/")}.${etw.entityName}{},
    </#list>
    )
    if err != nil {
        log.Fatalf("Failed to migrate database: %v", err)
    }
</#if>

    router := gin.Default()

    apiRouterGroup := router.Group("/api/v1")

<#list entitiesToWire as etw>
    ${etw.entityName?lower_case}Repo := ${repositoryPackagePath?keep_after_last("/")}.${etw.repositoryNewFunctionName}(db)
    ${etw.entityName?lower_case}Service := ${servicePackagePath?keep_after_last("/")}.${etw.serviceNewFunctionName}(${etw.entityName?lower_case}Repo)
    ${etw.entityName?lower_case}Handler := ${handlerPackagePath?keep_after_last("/")}.${etw.handlerNewFunctionName}(${etw.entityName?lower_case}Service)
    ${etw.entityName?lower_case}Handler.${etw.handlerSetupRoutesFunctionName}(apiRouterGroup)

</#list>

    serverPort := getEnv("${serverPortEnvVar}", "${defaultServerPort}")
    if err := router.Run(":" + serverPort); err != nil {
        log.Fatalf("Failed to start server: %v", err)
    }
}

func getEnv(key, defaultValue string) string {
    if value, exists := os.LookupEnv(key); exists {
        return value
    }
    return defaultValue
}
