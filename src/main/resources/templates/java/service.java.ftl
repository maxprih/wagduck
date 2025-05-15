package ${servicePackage};

<#list imports?sort as imp>
import ${imp};
</#list>

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ${serviceClassName} {

    private final ${repositoryClassName} ${repositoryVariableName};
    private final ${entityMapperName} ${entityMapperVariableName};

    <#if apiEndpoints?seq_contains("POST")>
    public ${responseDtoClassName} save(${requestDtoClassName} request) {
        ${entityClassName} ${entityClassName?lower_case} = ${entityMapperVariableName}.toEntity(request);
        return ${entityMapperVariableName}.toDto(${repositoryVariableName}.save(${entityClassName?lower_case}));
    }
    </#if>

    <#if apiEndpoints?seq_contains("PUT")>
    public ${responseDtoClassName} update(${primaryKeyType} ${primaryKeyName}, ${requestDtoClassName} request) {
        return ${repositoryVariableName}.findById(${primaryKeyName})
            .map(existing${entityClassName} -> {
                ${entityMapperVariableName}.update(existing${entityClassName}, request);
                return existing${entityClassName};
            })
            .map(${repositoryVariableName}::save)
            .map(${entityMapperVariableName}::toDto)
            .orElseThrow(() -> new ${resourceNotFoundExceptionName}("${entityClassName} not found with id: " + ${primaryKeyName}));
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET_LIST")>
    @Transactional(readOnly = true)
    public List<${responseDtoClassName}> getAll() {
        return ${repositoryVariableName}.findAll().stream()
            .map(${entityMapperVariableName}::toDto)
            .toList();
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET")>
    @Transactional(readOnly = true)
    public ${responseDtoClassName} get(${primaryKeyType} id) {
        return ${repositoryVariableName}.findById(id)
            .map(${entityMapperVariableName}::toDto)
            .orElseThrow(() -> new ${resourceNotFoundExceptionName}("${entityClassName} not found with id: " + id));
    }
    </#if>

    <#if apiEndpoints?seq_contains("DELETE")>
    public void delete(${primaryKeyType} id) {
        if (!${repositoryVariableName}.existsById(id)) {
             throw new ${resourceNotFoundExceptionName}("${entityClassName} not found with id: " + id);
        }
        ${repositoryVariableName}.deleteById(id);
    }
    </#if>
}
