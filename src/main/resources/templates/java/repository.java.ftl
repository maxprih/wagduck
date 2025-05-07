package ${packageName};

<#list imports?sort as imp>
import ${imp};
</#list>

@Repository
public interface ${interfaceName} extends JpaRepository<${entityClassName}, ${primaryKeyType}> {

}
