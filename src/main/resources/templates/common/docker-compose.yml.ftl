version: '3.8'

services:
  app:
    build: .
    container_name: ${appServiceName!"app"}-service
    ports:
      - "${appPort!8080}:${appPort!8080}"
<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL">
    environment:
<#if appLanguage == "JAVA" || appLanguage == "KOTLIN">
      DB_URL: ${appDbUrlEnvVar}
</#if>
      DB_USER: ${dbUser}
      DB_PASSWORD: ${dbPassword}
    depends_on:
      - ${dbServiceName!"db"}
</#if>

<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL">
  ${dbServiceName!"db"}:
    image: ${dbImage}
    container_name: ${appServiceName!"app"}-${dbServiceName!"db"}
    environment:
<#list dbEnvVars?keys as key>
      ${key}: ${dbEnvVars[key]}
</#list>
    ports:
      - "${dbPort}:${dbPort}"
    volumes:
      - ${dbVolumeName!"db-data"}:/var/lib/<#if databaseType == "POSTGRESQL">postgresql/data<#else>mysql</#if>
</#if>

<#-- Define Volumes only if DB exists -->
<#if databaseType == "POSTGRESQL" || databaseType == "MYSQL">
volumes:
  ${dbVolumeName!"db-data"}:
</#if>
