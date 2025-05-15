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

    <#if apiEndpoints?seq_contains("POST")>
    @PostMapping
    fun create(@RequestBody dto: ${requestDtoClassName}): ResponseEntity<${responseDtoClassName}> {
        val createdDto = ${serviceFieldName}.create(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDto)
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET")>
    @GetMapping("/{${primaryKeyName}}")
    fun getById(@PathVariable ${primaryKeyName}: ${primaryKeyType}): ResponseEntity<${responseDtoClassName}> {
        val dto = ${serviceFieldName}.findById(${primaryKeyName})
        return ResponseEntity.ok(dto)
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET_LIST")>
    @GetMapping
    fun getAll(): ResponseEntity<List<${responseDtoClassName}>> {
        val dtos = ${serviceFieldName}.findAll()
        return ResponseEntity.ok(dtos)
    }
    </#if>

    <#if apiEndpoints?seq_contains("PUT")>
    @PutMapping("/{${primaryKeyName}}")
    fun update(
        @PathVariable ${primaryKeyName}: ${primaryKeyType},
        @RequestBody dto: ${requestDtoClassName}
    ): ResponseEntity<${responseDtoClassName}> {
        val updatedDto = ${serviceFieldName}.update(${primaryKeyName}, dto)
        return ResponseEntity.ok(updatedDto)
    }
    </#if>

    <#if apiEndpoints?seq_contains("DELETE")>
    @DeleteMapping("/{${primaryKeyName}}")
    fun delete(@PathVariable ${primaryKeyName}: ${primaryKeyType}): ResponseEntity<Void> {
        ${serviceFieldName}.deleteById(${primaryKeyName})
        ResponseEntity.noContent().build()
    }
    </#if>
}
