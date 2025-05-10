package ${packageName}

<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>

</#if>
@Repository
interface ${repositoryName} : JpaRepository<${entityClassName}, ${primaryKeyType}> {

}
