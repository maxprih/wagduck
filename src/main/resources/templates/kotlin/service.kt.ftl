package ${packageName}

<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>

</#if>
@Service
@Transactional
class ${serviceClassName}(
    private val ${repositoryFieldName}: ${repositoryInterfaceName},
    private val ${mapperFieldName}: ${mapperInterfaceName}
) {

    fun create(dto: ${requestDtoClassName}): ${responseDtoClassName} {
        val entity = ${mapperFieldName}.toEntity(dto)
        val savedEntity = ${repositoryFieldName}.save(entity)
        return ${mapperFieldName}.toDto(savedEntity)
    }

    @Transactional(readOnly = true)
    fun findById(${primaryKeyName}: ${primaryKeyType}): ${responseDtoClassName} {
        val entity = ${repositoryFieldName}.findById(${primaryKeyName})
            .orElseThrow { ${entityNotFoundExceptionName}("${entityClassName} with id ${'$'}{${primaryKeyName}} not found") } <#-- Escape $ for FTL -->
        return ${mapperFieldName}.toDto(entity)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<${responseDtoClassName}> {
        return ${repositoryFieldName}.findAll()
            .map(${mapperFieldName}::toDto)
    }

    fun update(${primaryKeyName}: ${primaryKeyType}, dto: ${requestDtoClassName}): ${responseDtoClassName} {
        val existingEntity = ${repositoryFieldName}.findById(${primaryKeyName})
            .orElseThrow { ${entityNotFoundExceptionName}("${entityClassName} with id ${'$'}{${primaryKeyName}} not found to update") }

        ${mapperFieldName}.updateEntity(existingEntity, dto)
        val updatedEntity = ${repositoryFieldName}.save(existingEntity)
        return ${mapperFieldName}.toDto(updatedEntity)
    }

    fun deleteById(${primaryKeyName}: ${primaryKeyType}) {
        if (!${repositoryFieldName}.existsById(${primaryKeyName})) {
            throw ${entityNotFoundExceptionName}("${entityClassName} with id ${'$'}{${primaryKeyName}} not found to delete")
        }
        ${repositoryFieldName}.deleteById(${primaryKeyName})
    }
}
