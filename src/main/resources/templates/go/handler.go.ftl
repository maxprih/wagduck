package ${packageName}
<#assign allImports = ["github.com/gin-gonic/gin", "net/http"]>
<#-- Add service and model packages using their full paths from the model -->
<#assign allImports = allImports + [servicePackageName, entityPackageName]> <#-- These are now full paths like "module/service" -->
<#-- Collect specific imports based on handler function needs -->

<#-- Add other conditional imports from your model (if any were passed in 'imports' variable) -->
<#if imports?has_content>
    <#assign allImports = allImports + imports>
</#if>
<#assign uniqueImports = allImports?sort>
import (
<#list uniqueImports as imp>
    "${imp}"
</#list>
)

<#if description?has_content>
// ${description}
<#else>
// ${handlerStructName} handles HTTP requests for ${entityName} resources.
</#if>
type ${handlerStructName} struct {
    ${serviceFieldName} ${servicePackageName?keep_after_last("/")}.${serviceInterfaceName} <#-- Use base name for type -->
}

// New${handlerStructName} creates a new ${handlerStructName}.
func New${handlerStructName}(${serviceFieldName} ${servicePackageName?keep_after_last("/")}.${serviceInterfaceName}) *${handlerStructName} {
    return &${handlerStructName}{${serviceFieldName}: ${serviceFieldName}}
}

// Setup${entityName}Routes registers ${entityName?lower_case} routes with the Gin router.
func (${receiverName} *${handlerStructName}) Setup${entityName}Routes(router *gin.RouterGroup) {
    group := router.Group("${baseRoutePath}")
<#list routes as route>
    group.${route.httpMethod}("${route.path}", ${receiverName}.${route.handlerFunctionName})
</#list>
}

<#assign modelPkgQualifier = entityPackageName?keep_after_last('/') + ".">

<#list handlerFunctions as hf>

<#if hf.description?has_content>
// ${hf.name} ${hf.description}
<#else>
// ${hf.name} handles the request for ${hf.name?lower_case}.
</#if>
func (${receiverName} *${handlerStructName}) ${hf.name}(c *gin.Context) {
    <#assign ctx = "c.Request.Context()"> <#-- Gin context itself is often passed directly, or c.Request.Context() for service calls -->
    <#if hf.expectsRequestBody>
        <#-- hf.requestBodyType is now like "models.EntityName" -->
    var requestBody ${hf.requestBodyType}
    if err := c.ShouldBindJSON(&requestBody); err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body: " + err.Error()})
        return
    }
        <#-- Optional: For Create, explicitly nil out the ID if server should generate it -->
        <#if hf.name?matches("(?i).*Create.*") && hf.serviceParameterIdType?? && hf.serviceParameterIdType == "uuid.UUID">
            <#-- Assuming requestBodyType is a struct with an 'Id' field of type uuid.UUID -->
        </#if>
    </#if>

    <#assign finalIdVarName = "">
    <#if hf.hasPathParameter>
        <#assign pathParamVar = hf.pathParameterName> <#-- e.g., "id" -->
        <#assign finalIdVarName = "final" + pathParamVar?cap_first> <#-- e.g., "finalId" -->
    ${pathParamVar}Str := c.Param("${pathParamVar}")
        <#-- Check the type the SERVICE expects for the ID -->
        <#if hf.serviceParameterIdType?? && hf.serviceParameterIdType == "uuid.UUID">
    ${finalIdVarName}, err := uuid.Parse(${pathParamVar}Str)
    if err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ${pathParamVar} format (UUID expected): " + err.Error()})
        return
    }
        <#elseif hf.pathParameterType?? && (hf.pathParameterType == "int64" || hf.pathParameterType == "int")> <#-- hf.pathParameterType is for parsing -->
    parsed${pathParamVar?cap_first}, err := strconv.ParseInt(${pathParamVar}Str, 10, 64)
    if err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid ${pathParamVar} format (integer expected): " + err.Error()})
        return
    }
            <#if hf.serviceParameterIdType?? && hf.serviceParameterIdType == "uint">
    ${finalIdVarName} := uint(parsed${pathParamVar?cap_first}) <#-- Convert to uint if service expects uint -->
            <#elseif hf.serviceParameterIdType?? && hf.serviceParameterIdType == "int">
    ${finalIdVarName} := int(parsed${pathParamVar?cap_first}) <#-- Convert to int if service expects int -->
            <#else>
    ${finalIdVarName} := parsed${pathParamVar?cap_first} <#-- Already int64 or service expects int64 -->
            </#if>
        <#elseif hf.pathParameterType?? && hf.pathParameterType == "string"> <#-- Service expects a plain string ID -->
    ${finalIdVarName} := ${pathParamVar}Str
        <#else>
    ${finalIdVarName} := ${pathParamVar}Str
    c.Logger().Warnf("Path parameter '${pathParamVar}' for handler '${hf.name}' has unhandled type combination: pathParamType='${hf.pathParameterType!'-'}', serviceIdType='${hf.serviceParameterIdType!'-'}'. Defaulting to string.")
        </#if>
    </#if>

    <#-- Construct service call arguments -->
    <#assign serviceCallArgs = [ctx]>
    <#if hf.hasPathParameter && finalIdVarName != "">
        <#assign serviceCallArgs = serviceCallArgs + [finalIdVarName]>
    </#if>
    <#if hf.expectsRequestBody>
        <#assign serviceCallArgs = serviceCallArgs + ["&requestBody"]>
    </#if>
    <#if hf.successStatusCode == "http.StatusNoContent" || hf.name?matches("(?i).*Delete.*")>
    err = ${receiverName}.${serviceFieldName}.${hf.serviceMethodName}(${serviceCallArgs?join(", ")})
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to ${hf.name?lower_case}: " + err.Error()})
        return
    }
    c.Status(http.StatusNoContent)
    <#else>
    result, err := ${receiverName}.${serviceFieldName}.${hf.serviceMethodName}(${serviceCallArgs?join(", ")})
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to ${hf.name?lower_case}: " + err.Error()})
        return
    }
    c.JSON(${hf.successStatusCode!"http.StatusOK"}, result)
    </#if>
}

</#list>