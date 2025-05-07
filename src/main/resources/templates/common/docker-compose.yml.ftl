version: '3.8'

services:
  app:
    build: . <#-- Build from the Dockerfile in the current directory -->
    container_name: ${appServiceName!"app"}-service
    ports:
      - "${appPort!8080}:${appPort!8080}" <#-- Map host port to container port -->
<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL"> <#-- Only add DB env vars if DB exists -->
    environment:
      SPRING_DATASOURCE_URL: ${appDbUrlEnvVar}
      SPRING_DATASOURCE_USERNAME: ${dbUser}
      SPRING_DATASOURCE_PASSWORD: ${dbPassword}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update <#-- Or 'validate' if using migrations -->
      # Add other environment variables needed by the app
      # Example: SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - ${dbServiceName!"db"} <#-- Wait for the db service to start -->
</#if>

<#-- Define Database Service only if needed -->
<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL">
  ${dbServiceName!"db"}:
    image: ${dbImage}
    container_name: ${appServiceName!"app"}-${dbServiceName!"db"}
    environment:
<#list dbEnvVars?keys as key>
      ${key}: ${dbEnvVars[key]}
</#list>
    ports:
      - "${dbPort}:${dbPort}" <#-- Expose DB port to host for development access -->
    volumes:
      - ${dbVolumeName!"db-data"}:/var/lib/<#if databaseType == "POSTGRESQL">postgresql/data<#else>mysql</#if> <#-- Persist data -->
    # Optional: Add healthcheck for more robust startup dependency
    # healthcheck:
    #   test: ["CMD-SHELL", "pg_isready -U ${dbUser} -d ${dbName}" | "mysqladmin ping -h localhost -u ${dbUser} -p${dbPassword}"]
    #   interval: 10s
    #   timeout: 5s
    #   retries: 5
</#if>

<#-- Define Volumes only if DB exists -->
<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL">
volumes:
  ${dbVolumeName!"db-data"}:
</#if>
