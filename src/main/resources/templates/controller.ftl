package ${packageName}.controller;

import ${packageName}.model.${entityName};
import ${packageName}.service.${entityName}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/${entityName?lower_case}")
@RequiredArgsConstructor
public class ${entityName}Controller {

    private final ${entityName}Service ${entityName?lower_case}Service;

    @GetMapping
    public ResponseEntity<Page<${entityName}>> getAll(Pageable pageable) {
        ${beb.name}
        ${beb.age}
        return ResponseEntity.ok(${entityName?lower_case}Service.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<${entityName}> getById(@PathVariable Long id) {
        return ${entityName?lower_case}Service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

<#--    @PostMapping-->
<#--    public ResponseEntity<${entityName}> create(@Valid @RequestBody ${entityName} ${entityName?uncap_first}) {-->
<#--        ${entityName} createdEntity = ${entityName?uncap_first}Service.create(${entityName?uncap_first});-->
<#--        URI location = ServletUriComponentsBuilder.fromCurrentRequest()-->
<#--                .path("/{id}")-->
<#--                .buildAndExpand(createdEntity.getId())-->
<#--                .toUri();-->
<#--        return ResponseEntity.created(location).body(createdEntity);-->
<#--    }-->

    @PutMapping("/{id}")
    public ResponseEntity<${entityName}> update(
            @PathVariable Long id,
            @Valid @RequestBody ${entityName} ${entityName?uncap_first}) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.update(id, ${entityName?uncap_first}));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ${entityName?uncap_first}Service.delete(id);
    }
}