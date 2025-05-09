package main

import (
    "log"
    "os"
    "net/http"
    "time"

    "github.com/gin-gonic/gin"

    "${moduleName}${configPackagePath}"
<#if autoMigrateEntities>
    "${moduleName}${modelsPackagePath}"
</#if>
    "${moduleName}${repositoryPackagePath}"
    "${moduleName}${servicePackagePath}"
    "${moduleName}${handlerPackagePath}"

<#-- Additional imports if needed, e.g. for a specific logger -->
<#if imports?has_content>
    <#list imports?sort as imp>
    "${imp}"
    </#list>
</#if>
)

func main() {
    // Load application configurations
    log.Println("Loading configurations...")
    appConfig := ${configPackagePath?keep_after_last("/")}.LoadDatabaseConfig() // Assuming db config also has server port or we add another config loader
    // serverPort := getEnv("${serverPortEnvVar}", "${defaultServerPort}") // Or load from a general app config

    // Initialize database connection
    log.Println("Initializing database connection...")
    db, err := ${configPackagePath?keep_after_last("/")}.InitDatabaseConnection(appConfig)
    if err != nil {
        log.Fatalf("Failed to connect to database: %v", err)
    }
    log.Println("Database connection successful.")

<#if autoMigrateEntities && entitiesToWire?has_content>
    // Auto-migrate database schemas
    log.Println("Running database migrations...")
    err = db.AutoMigrate(
    <#list entitiesToWire as etw>
        &${modelsPackagePath?keep_after_last("/")}.${etw.entityName}{},
    </#list>
    )
    if err != nil {
        log.Fatalf("Failed to migrate database: %v", err)
    }
    log.Println("Database migration successful.")
</#if>

    // Initialize Gin router
    log.Println("Setting up Gin router...")
    router := gin.Default()
    // router.Use(gin.Logger()) // Default logger
    // router.Use(gin.Recovery()) // Default recovery middleware

    // Health check endpoint
    router.GET("/health", func(c *gin.Context) {
        c.JSON(http.StatusOK, gin.H{"status": "UP", "timestamp": time.Now().UTC().Format(time.RFC3339)})
    })

    apiRouterGroup := router.Group("/api/v1") // Base path for API routes

    // Initialize layers (repositories, services, handlers)
    log.Println("Initializing application layers...")
<#list entitiesToWire as etw>
    // --- ${etw.entityName} Wiring ---
    ${etw.entityName?lower_case}Repo := ${repositoryPackagePath?keep_after_last("/")}.${etw.repositoryNewFunctionName}(db)
    ${etw.entityName?lower_case}Service := ${servicePackagePath?keep_after_last("/")}.${etw.serviceNewFunctionName}(${etw.entityName?lower_case}Repo)
    ${etw.entityName?lower_case}Handler := ${handlerPackagePath?keep_after_last("/")}.${etw.handlerNewFunctionName}(${etw.entityName?lower_case}Service)
    ${etw.entityName?lower_case}Handler.${etw.handlerSetupRoutesFunctionName}(apiRouterGroup)
    log.Printf("${etw.entityName} routes registered.")

</#list>

    // Start the HTTP server
    serverPort := getEnv("${serverPortEnvVar}", "${defaultServerPort}")
    log.Printf("Starting server on port %s...", serverPort)
    if err := router.Run(":" + serverPort); err != nil {
        log.Fatalf("Failed to start server: %v", err)
    }
}

// Helper function to get an environment variable or return a default value.
func getEnv(key, defaultValue string) string {
    if value, exists := os.LookupEnv(key); exists {
        return value
    }
    log.Printf("Environment variable %s not set, using default value: %s", key, defaultValue)
    return defaultValue
}
