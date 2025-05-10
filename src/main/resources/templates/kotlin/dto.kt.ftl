package ${packageName}

<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>

</#if>
data class ${className}(
<#list attributes as attr>
    val ${attr.name}: ${attr.baseKotlinType}<#if attr.nullable && !(attr.baseKotlinType?ends_with("?"))>?</#if>,
</#list>
<#list relationships as rel>
    val ${rel.name}: <#if rel.collection>${rel.collectionType!"Set"}<${rel.relatedDtoClassName}></#if><#if !rel.collection>${rel.relatedDtoClassName}</#if><#if rel.nullable && !rel.collection>?</#if><#if rel.initializer??> = ${rel.initializer}</#if><#if !rel?is_last >,</#if>
</#list>
)
