package ${packageName}

<#assign allImports = []>
<#if imports?has_content>
    <#assign allImports = allImports + imports>
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
// ${interfaceName} defines the interface for ${entityName?lower_case} data operations.
</#if>
type ${interfaceName} interface {
<#list methods as method>
    <#if method.description?has_content>
    // ${method.name} ${method.description}
    </#if>
    ${method.name}(<#list method.parameters as param>${param.name} ${param.type}<#if param?has_next>, </#if></#list>) (<#list method.returnTypes as rt>${rt.type}<#if rt?has_next>, </#if></#list>)
</#list>
}