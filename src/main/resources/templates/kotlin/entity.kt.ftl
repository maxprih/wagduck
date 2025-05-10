package ${packageName}

<#-- Generate imports -->
<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>

</#if>
<#list classAnnotations as ann>
${ann}
</#list>
open class ${className} {
<#assign pkAttribute = {} >
<#list attributes as attr>
    <#if attr.primaryKey>
        <#assign pkAttribute = attr>
        <#break>
    </#if>
</#list>

<#list attributes as attr>
    <#if attr.description?? && attr.description?has_content>
    /**
     * ${attr.description}
     */
    </#if>
    <#list attr.annotations as ann>
    ${ann}
    </#list>
    var ${attr.name}: ${attr.baseKotlinType}<#if attr.nullable && !(attr.baseKotlinType?ends_with("?"))>?</#if><#if attr.initializer??> = ${attr.initializer}<#elseif attr.nullable> = null</#if>

</#list>
<#-- Generate relationships -->
<#list relationships as rel>
    <#if rel.description?? && rel.description?has_content>
    /**
     * ${rel.description}
     */
    </#if>
    <#list rel.annotations as ann>
    ${ann}
    </#list>
    var ${rel.name}: ${rel.baseKotlinType}<#if rel.nullable && !(rel.baseKotlinType?ends_with("?")) && !(rel.baseKotlinType?matches(".*<.*\\?>"))>?</#if><#if rel.initializer??> = ${rel.initializer}<#elseif rel.nullable> = null</#if>

</#list>
<#-- Generate Auditing Fields if enabled -->
<#if includeAuditing && createdAtAttribute??>
    <#if createdAtAttribute.description?? && createdAtAttribute.description?has_content>
    /**
     * ${createdAtAttribute.description!"Creation timestamp."}
     */
    </#if>
    <#list createdAtAttribute.annotations as ann>
    ${ann}
    </#list>
    var ${createdAtAttribute.name}: ${createdAtAttribute.baseKotlinType}<#if createdAtAttribute.nullable && !(createdAtAttribute.baseKotlinType?ends_with("?"))>?</#if><#if createdAtAttribute.initializer??> = ${createdAtAttribute.initializer}<#else> = null</#if>
</#if>
<#if includeAuditing && updatedAtAttribute??>
    <#if updatedAtAttribute.description?? && updatedAtAttribute.description?has_content>
    /**
     * ${updatedAtAttribute.description!"Last update timestamp."}
     */
    </#if>
    <#list updatedAtAttribute.annotations as ann>
    ${ann}
    </#list>
    var ${updatedAtAttribute.name}: ${updatedAtAttribute.baseKotlinType}<#if updatedAtAttribute.nullable && !(updatedAtAttribute.baseKotlinType?ends_with("?"))>?</#if><#if updatedAtAttribute.initializer??> = ${updatedAtAttribute.initializer}<#else> = null</#if>
</#if>

<#if pkAttribute.name??>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ${className}) return false

        return ${pkAttribute.name} != null && this.${pkAttribute.name} == other.${pkAttribute.name}
    }

    override fun hashCode(): Int {
        return ${pkAttribute.name}?.hashCode() ?: javaClass.hashCode()
    }

    override fun toString(): String {
        return "${className}(${pkAttribute.name}=${'$'}{${pkAttribute.name}})"
    }
<#else>
    // No primary key found for equals/hashCode generation.
    // Consider using business key or default to superclass behavior if applicable.
    // override fun equals(other: Any?): Boolean = super.equals(other)
    // override fun hashCode(): Int = super.hashCode()
    // override fun toString(): String = "${className}(transient)"
</#if>
}
