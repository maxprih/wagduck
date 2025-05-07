package ${packageName};

<#list imports?sort as imp>
import ${imp};
</#list>

@Mapper(componentModel = "${componentModel}")
public interface ${interfaceName} {

<#list toEntityMappings as mapping>
    ${mapping}
</#list>
    ${entityClassName} toEntity(${requestDtoClassName} requestDto);

<#list toDtoMappings as mapping>
    ${mapping}
</#list>
    ${responseDtoClassName} toDto(${entityClassName} entity);

<#list updateMappings as mapping>
    ${mapping}
</#list>
    void update(@MappingTarget ${entityClassName} existingEntity, ${requestDtoClassName} requestDto);
}
