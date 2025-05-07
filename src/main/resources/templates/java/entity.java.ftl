package ${packageName};

<#-- Generate imports -->
<#list imports?sort as imp>
import ${imp};
</#list>

<#-- Generate class annotations -->
<#list classAnnotations as ann>
${ann}
</#list>
public class ${className} {

<#-- Generate attributes -->
<#list attributes as attr>
    <#list attr.annotations as ann>
    ${ann}
    </#list>
    private ${attr.type} ${attr.name};

</#list>
<#-- Generate relationships -->
<#list relationships as rel>
    <#list rel.annotations as ann>
    ${ann}
    </#list>
    private ${rel.type} ${rel.name}<#if rel.type?starts_with("Set<")> = new HashSet<>()</#if><#if rel.type?starts_with("List<")> = new ArrayList<>()</#if>;

</#list>
<#-- Generate Auditing Fields if enabled and not handled by Lombok/Superclass -->
<#if includeAuditing && createdAtAttribute?? >
    /**
    * Creation timestamp.
    */
    <#list createdAtAttribute.annotations as ann>
    ${ann}
    </#list>
    private ${createdAtAttribute.type} ${createdAtAttribute.name};
</#if>
<#if includeAuditing && updatedAtAttribute?? >

    /**
    * Last update timestamp.
    */
    <#list updatedAtAttribute.annotations as ann>
    ${ann}
    </#list>
    private ${updatedAtAttribute.type} ${updatedAtAttribute.name};
</#if>
}
