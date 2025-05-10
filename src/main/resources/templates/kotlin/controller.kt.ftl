package ${packageName}

<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>

</#if>
@RestController
@RequestMapping("${baseRequestPath}")
class ${controllerClassName}(
    private val ${serviceFieldName}: ${serviceClassName}
) {

    @PostMapping
    fun create(@RequestBody dto: ${requestDtoClassName}): ResponseEntity<${responseDtoClassName}> {
        val createdDto = ${serviceFieldName}.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto)
    }

    @GetMapping("/{${primaryKeyName}}")
    fun getById(@PathVariable ${primaryKeyName}: ${primaryKeyType}): ResponseEntity<${responseDtoClassName}> {
        val dto = ${serviceFieldName}.findById(${primaryKeyName})
        return ResponseEntity.ok(dto)
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<${responseDtoClassName}>> {
        val dtos = ${serviceFieldName}.findAll()
        return ResponseEntity.ok(dtos)
    }

    @PutMapping("/{${primaryKeyName}}")
    fun update(
        @PathVariable ${primaryKeyName}: ${primaryKeyType},
        @RequestBody dto: ${requestDtoClassName}
    ): ResponseEntity<${responseDtoClassName}> {
        val updatedDto = ${serviceFieldName}.update(${primaryKeyName}, dto)
        return ResponseEntity.ok(updatedDto)
    }

    @DeleteMapping("/{${primaryKeyName}}")
    fun delete(@PathVariable ${primaryKeyName}: ${primaryKeyType}): ResponseEntity<Void> {
        return try {
            ${serviceFieldName}.deleteById(${primaryKeyName})
            ResponseEntity.noContent().build()
        } catch (e: ${entityNotFoundExceptionName}) {
            // Optionally log: log.warn("Attempted to delete non-existent ${entityClassNameForLogging} with id ${'$'}{${primaryKeyName}}")
            ResponseEntity.notFound().build() // Or noContent() if idempotent delete is preferred even for non-existent
        }
    }
}