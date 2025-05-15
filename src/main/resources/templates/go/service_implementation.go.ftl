package ${packageName}

<#assign allImports = []>
<#if imports?has_content>
    <#assign allImports = allImports + imports>
</#if>

<#assign needsUUIDImportForUpdate = false>
<#assign needsTimeImportForUpdate = false>
<#assign needsSqlImportForUpdate = false>
<#list methods as method>
    <#if method.name?matches("(?i).*Update.*") && method.updatableFields?has_content>
        <#list method.updatableFields as field>
            <#if field.goFieldType == "uuid.UUID">
                <#assign needsUUIDImportForUpdate = true>
            </#if>
            <#if field.goFieldType == "time.Time">
                <#assign needsTimeImportForUpdate = true>
            </#if>
            <#if field.goFieldType?starts_with("sql.Null")> <#-- e.g. sql.NullString -->
                <#assign needsSqlImportForUpdate = true>
            </#if>
        </#list>
    </#if>
</#list>

<#if needsUUIDImportForUpdate>
    <#assign allImports = allImports + ["github.com/google/uuid"]>
</#if>
<#if needsTimeImportForUpdate>
    <#assign allImports = allImports + ["time"]>
</#if>
<#if needsSqlImportForUpdate>
    <#assign allImports = allImports + ["database/sql"]>
</#if>

<#assign uniqueImports = allImports?sort>
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
// ${structName} implements ${interfaceName}.
</#if>
type ${structName} struct {
    ${repositoryFieldName} ${repositoryPackageName?keep_after_last("/")}.${repositoryInterfaceName}
    // logger *zap.Logger // Example: if you have a logger
}

// New${entityName}Service creates a new instance of ${structName}.
func New${entityName}Service(${repositoryFieldName} ${repositoryPackageName?keep_after_last("/")}.${repositoryInterfaceName}) ${interfaceName} {
    return &${structName}{
        ${repositoryFieldName}: ${repositoryFieldName},
    }
}

<#list methods as method>
func (${receiverName} *${structName}) ${method.name}(<#list method.parameters as param>${param.name} ${param.type}<#if param?has_next>, </#if></#list>) (<#list method.returnTypes as rtType>${rtType.name!"_"}<#if rtType.name??> </#if>${rtType.type}<#if rtType?has_next>, </#if></#list>) {
    <#assign firstParamName = method.parameters[0].name> <#-- usually ctx -->
    <#assign secondParam = method.parameters[1]!"">

    <#if method.name == "Create${entityName}" || method.name == "Create">
    return ${receiverName}.${repositoryFieldName}.${method.correspondingRepositoryMethodName}(${firstParamName}, ${secondParam.name})
    <#elseif method.name == "Get${entityName}ByID" || method.name == "GetByID">
        <#-- Business logic for GetByID: authorization, specific error mapping -->
    result, err := ${receiverName}.${repositoryFieldName}.${method.correspondingRepositoryMethodName}(${firstParamName}, ${secondParam.name})
    if err != nil {
        return nil, err
    }
    // if result == nil { // If repository might return (nil, nil) for not found
    //    return nil, YourCustomNotFoundError
    // }
    return result, nil
    <#elseif method.name == "Update${entityName}" || method.name == "Update">
        <#assign ctxParamName = method.parameters[0].name>
        <#assign idParam = method.parameters[1]>
        <#assign detailsParam = method.parameters[2]> <#-- This is the *models.Entity type -->
        <#assign existingEntityVar = "existing" + entityName>
        <#assign idParamName = idParam.name>
        <#assign detailsParamName = detailsParam.name>

    // 1. Fetch the existing ${entityName?lower_case}.
    // Assuming repository has GetByID or a similar method that is suitable here.
    // If your service layer's Get${entityName}ByID has important logic (e.g. auth), consider calling that.
    ${existingEntityVar}, err := ${receiverName}.${repositoryFieldName}.GetByID(${ctxParamName}, ${idParamName})
    if err != nil {
        // TODO: Map to specific errors like NotFoundError if applicable
        return nil, err
    }
    // if ${existingEntityVar} == nil { // If GetByID can return (nil,nil) for not found
    //    return nil, YourCustomNotFoundError // Or an error indicating resource not found
    // }

    // 2. Apply updates from ${detailsParamName} to ${existingEntityVar} only if present in request.
    // "Present in request" for value types is checked by comparing against their zero value.
    // For pointer types within the model, it's checked against nil.
    <#if method.updatableFields?has_content>
        <#list method.updatableFields as field>
            <#assign fieldNameInDetails = detailsParamName + "." + field.goFieldName>
            <#assign fieldNameInExisting = existingEntityVar + "." + field.goFieldName>
            <#assign fieldType = field.goFieldType>

            <#if fieldType == "string">
    if ${fieldNameInDetails} != "" {
        ${fieldNameInExisting} = ${fieldNameInDetails}
    }
            <#elseif fieldType == "int" || fieldType == "int32" || fieldType == "int64" ||
                     fieldType == "uint" || fieldType == "uint32" || fieldType == "uint64" ||
                     fieldType == "float32" || fieldType == "float64" ||
                     fieldType == "byte" || fieldType == "rune">
    if ${fieldNameInDetails} != 0 {
        ${fieldNameInExisting} = ${fieldNameInDetails}
    }
            <#elseif fieldType == "bool">
    // For boolean fields from a request model (not a DTO with *bool),
    // the value is always present (either true or false).
    // If the intent is "update if the client provided this field", and the field is bool (not *bool),
    // then we typically assign it directly. The "check for presence" is implicitly that it's an updatable field.
    // If you need to distinguish "not provided" from "set to false", the field in 'detailsParamName'
    // (i.e., in your model.Entity struct) should be a pointer type like *bool.
    ${fieldNameInExisting} = ${fieldNameInDetails}
            <#elseif fieldType == "uuid.UUID">
    if ${fieldNameInDetails} != uuid.Nil { // Requires "github.com/google/uuid" import
        ${fieldNameInExisting} = ${fieldNameInDetails}
    }
            <#elseif fieldType == "time.Time">
    if !${fieldNameInDetails}.IsZero() { // Requires "time" import
        ${fieldNameInExisting} = ${fieldNameInDetails}
    }
            <#elseif fieldType?starts_with("*")> <#-- Model field is a pointer e.g. *string, *int, *time.Time, *uuid.UUID -->
    if ${fieldNameInDetails} != nil { <#-- Check if the pointer in the request model is not nil -->
        <#-- Assign the value pointed to if existing field is a value type,
             or assign the pointer itself if existing field is also a pointer of the same type.
             Assuming existing field is a value type for common update patterns.
             If existingEntityVar.FieldName is also a pointer, it would be:
             // ${fieldNameInExisting} = ${fieldNameInDetails}
             For now, assuming existing field is a value type for simplicity:
        -->
        ${fieldNameInExisting} = *${fieldNameInDetails}
    }
            <#elseif fieldType?starts_with("sql.Null")> <#-- e.g. sql.NullString, sql.NullInt64. Model field uses sql.NullXXX -->
    if ${fieldNameInDetails}.Valid { // Requires "database/sql" import
        <#-- This assumes existingEntityVar.FieldName is also a sql.NullXXX type.
             If existingEntityVar.FieldName is a basic type (e.g. string, int64), you'd use:
             // ${fieldNameInExisting} = ${fieldNameInDetails}.String  (or .Int64, .Bool etc.)
        -->
        ${fieldNameInExisting} = ${fieldNameInDetails}
    }
            <#else>
    // Fallback for other unhandled types: direct assignment.
    // This was the original behavior. Add specific checks above if needed.
    ${fieldNameInExisting} = ${fieldNameInDetails}
            </#if>
        </#list>
    <#else>
    // WARNING: No updatable fields were defined for ${entityName}.
    // The ${detailsParamName} object was not used to update ${existingEntityVar}.
    // Check your GoServiceMapper to ensure 'updatableFields' are populated correctly.
    </#if>

    // 3. Optional: Add any other business logic, validation on the merged existingEntityVar.
    // Example:
    // ${existingEntityVar}.UpdatedAt = time.Now()
    // if err := ${existingEntityVar}.Validate(); err != nil {
    //    return nil, err // Or a custom validation error
    // }

    // 4. Save the updated entity.
    return ${receiverName}.${repositoryFieldName}.${method.correspondingRepositoryMethodName}(${ctxParamName}, ${existingEntityVar})
    <#elseif method.name == "Delete${entityName}" || method.name == "Delete">
        <#-- Business logic for Delete: e.g., check if deletable, soft delete logic -->
    return ${receiverName}.${repositoryFieldName}.${method.correspondingRepositoryMethodName}(${firstParamName}, ${secondParam.name})
    <#elseif method.name == "List${entityName}s" || method.name == "List">
        <#-- Business logic for List: e.g., applying default filters, pagination options -->
        <#assign listArgs = [firstParamName]>
        <#if method.parameters?size gt 1> <#-- If List method takes additional params (e.g., filter options) -->
            <#list method.parameters[1..] as p>
                <#assign listArgs = listArgs + [p.name]>
            </#list>
        </#if>
    return ${receiverName}.${repositoryFieldName}.${method.correspondingRepositoryMethodName}(${listArgs?join(", ")})
    <#else>
    // TODO: Implement business logic for ${method.name}
    panic("service method ${method.name} not fully implemented")
    </#if>
}
</#list>