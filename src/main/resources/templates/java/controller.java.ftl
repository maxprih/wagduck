package ${controllerPackage};

<#list imports as imp>
import ${imp};
</#list>

/**
 * REST controller for managing {@link ${entityPackage}.${entityClassName}}.
 */
@RestController
@RequestMapping("${basePath}")
@RequiredArgsConstructor
public class ${entityClassName}Controller {

    private final ${serviceClassName} ${serviceVariableName};

    <#if apiEndpoints?seq_contains("POST")>
    /**
     * Create a new {@link ${entityPackage}.${entityClassName}}.
     *
     * @param request the {@link ${entityPackage}.${entityClassName}} to create.
     * @return new {@link ${responseDtoClassName}}.
     */
    @PostMapping
    public ResponseEntity<${responseDtoClassName}> create(@RequestBody ${requestDtoClassName} request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(${serviceVariableName}.save(request));
    }
    </#if>

    <#if apiEndpoints?seq_contains("PUT")>
    /**
     * Updates an existing {@link ${entityPackage}.${entityClassName}}.
     *
     * @param ${primaryKeyName} the id of the {@link ${responseDtoClassName}} to save.
     * @param request the {@link ${entityPackage}.${entityClassName}} to update.
     * @return updated {@link ${responseDtoClassName}},
     */
    @PutMapping("/{${primaryKeyName}}")
    public ResponseEntity<${responseDtoClassName}> update(
            @PathVariable ${primaryKeyType} ${primaryKeyName},
            @RequestBody ${requestDtoClassName} request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(${serviceVariableName}.update(${primaryKeyName}, request));
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET_LIST")>
    /**
     * Get all the {@link ${entityPackage}.${entityClassName}}.
     *
     * @return list of {@link ${responseDtoClassName}}.
     */
    @GetMapping
    public ResponseEntity<List<${responseDtoClassName}>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(${serviceVariableName}.getAll());
    }
    </#if>

    <#if apiEndpoints?seq_contains("GET")>
    /**
     * Get the {@link ${entityPackage}.${entityClassName}} by id.
     *
     * @param ${primaryKeyName} the id of the {@link ${responseDtoClassName}} to retrieve.
     * @return {@link ${responseDtoClassName}}.
     */
    @GetMapping("/{${primaryKeyName}}")
    public ResponseEntity<${responseDtoClassName}> get(@PathVariable ${primaryKeyType} ${primaryKeyName}) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(${serviceVariableName}.get(${primaryKeyName}));
    }
    </#if>

    <#if apiEndpoints?seq_contains("DELETE")>
    /**
     * Delete the {@link ${entityPackage}.${entityClassName}} by id.
     *
     * @param ${primaryKeyName} the id of the {@link ${responseDtoClassName}} to delete.
     */
    @DeleteMapping("/{${primaryKeyName}}")
    public ResponseEntity<Void> delete(@PathVariable ${primaryKeyType} ${primaryKeyName}) {
        ${serviceVariableName}.delete(${primaryKeyName});
        return ResponseEntity.noContent().build();
    }
    </#if>
}
