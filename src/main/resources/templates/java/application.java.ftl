package ${packageName};

<#list imports?sort as imp>
import ${imp};
</#list>

<#list annotations as ann>
${ann}
</#list>
public class ${className} {
    public static void main(String[] args) {
        SpringApplication.run(${className}.class, args);
    }
}