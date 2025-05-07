package ${packageName};

<#list imports?sort as imp>
import ${imp};
</#list>

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${className} {

    <#list attributes as attr>
        <#if attr.description??>
    /**
     * ${attr.description?replace("\n", "\n     * ")}
     */
        </#if>
        <#list attr.annotations as ann>
    ${ann}
        </#list>
    private ${attr.type} ${attr.name};

    </#list>
}