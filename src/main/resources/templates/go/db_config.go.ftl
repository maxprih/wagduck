package ${packageName}

<#assign uniqueImports = imports?sort>
<#if uniqueImports?has_content>
import (
    <#list uniqueImports as imp>
    "${imp}"
    </#list>
)
</#if>

<#if description?has_content>
// ${description}
<#else>
// ${configStructName} holds the database connection parameters.
</#if>
type ${configStructName} struct {
<#list fields as field>
    ${field.fieldName} ${field.fieldType} <#if field.sensitive>`json:"-"` <#else>`json:"${field.fieldName?lower_case}"`</#if>
</#list>
}

<#if loadFunctionName?has_content>
// ${loadFunctionName} loads database configuration from environment variables.
func ${loadFunctionName}() *${configStructName} {
    cfg := &${configStructName}{
    <#list fields as field>
        <#if field.fieldType == "string">
        ${field.fieldName}: getEnv("${field.envVarName}", "${field.defaultValue ! ''}"),
        <#elseif field.fieldType == "int">
        ${field.fieldName}: getEnvAsInt("${field.envVarName}", ${field.defaultValue ! '0'}),
        </#if>
    </#list>
    }

    <#assign dbNameField = fields?filter(f -> f.fieldName == "DBName")?first!"">
    <#if dbNameField?has_content>
    if cfg.${dbNameField.fieldName} == "" {
        log.Println("Warning: ${dbNameField.envVarName} is not set, database name might be missing.")
    }
    </#if>
     <#assign userField = fields?filter(f -> f.fieldName == "User")?first!"">
    <#if userField?has_content>
    if cfg.${userField.fieldName} == "" {
        log.Println("Warning: ${userField.envVarName} is not set, database user might be missing.")
    }
    </#if>

    return cfg
}
</#if>

<#if dsnFunctionName?has_content>
// ${dsnFunctionName} returns the DSN string for connecting to the PostgreSQL database.
func (cfg *${configStructName}) ${dsnFunctionName}() string {
    <#assign dsnParts = []>
    <#list fields as field>
        <#if field.fieldName == "Host" && field.fieldType == "string">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"host=%s\", cfg.Host)"]>
        <#elseif field.fieldName == "Port" && field.fieldType == "int">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"port=%d\", cfg.Port)"]>
        <#elseif field.fieldName == "User" && field.fieldType == "string">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"user=%s\", cfg.User)"]>
        <#elseif field.fieldName == "Password" && field.fieldType == "string">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"password=%s\", cfg.Password)"]>
        <#elseif field.fieldName == "DBName" && field.fieldType == "string">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"dbname=%s\", cfg.DBName)"]>
        <#elseif field.fieldName == "SSLMode" && field.fieldType == "string">
            <#assign dsnParts = dsnParts + ["fmt.Sprintf(\"sslmode=%s\", cfg.SSLMode)"]>
        </#if>
    </#list>
    <#if dsnParts?has_content>
    return strings.Join([]string{
        <#list dsnParts as part>
        ${part},
        </#list>
    }, " ")
    <#else>
    return ""
    </#if>
}
</#if>

<#if initDbFunctionName?has_content>
// ${initDbFunctionName} initializes and returns a GORM DB connection instance.
func ${initDbFunctionName}(cfg *${configStructName}) (*gorm.DB, error) {
    dsn := cfg.DSN()
    if dsn == "" {
        return nil, fmt.Errorf("database DSN is empty, please check configuration")
    }

    db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})

    if err != nil {
        return nil, fmt.Errorf("failed to connect to database: %w", err)
    }

    log.Println("Successfully connected to the database!")
    return db, nil
}
</#if>

// Helper function to get an environment variable or return a default value.
func getEnv(key, defaultValue string) string {
    if value, exists := os.LookupEnv(key); exists {
        return value
    }
    return defaultValue
}

// Helper function to get an environment variable as an integer or return a default value.
func getEnvAsInt(key string, defaultValue int) int {
    valueStr := getEnv(key, "")
    if value, err := strconv.Atoi(valueStr); err == nil {
        return value
    }
    if defaultValue != 0 && valueStr == "" {
         log.Printf("Warning: Environment variable %s not set or invalid, using default value %d\n", key, defaultValue)
    } else if valueStr != "" {
        log.Printf("Warning: Invalid value for %s: %s. Using default %d\n", key, valueStr, defaultValue)
    }
    return defaultValue
}