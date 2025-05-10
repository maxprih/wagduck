package ${packageName}

<#if imports?has_content>
    <#list imports?sort as imp>
import ${imp}
    </#list>
</#if>

@Mapper(
    componentModel = "spring"<#if usesMapperNames?has_content && usesMapperNames?size gt 0>,
    uses = [
        <#list usesMapperNames as usedMapper>${usedMapper}::class<#if usedMapper?has_next>,</#if></#list>
    ]</#if>
)
interface ${mapperName} {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun toEntity(dto: ${requestDtoClassName}): ${entityClassName}

    fun toDto(entity: ${entityClassName}): ${responseDtoClassName}

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun updateEntity(@MappingTarget entity: ${entityClassName}, dto: ${requestDtoClassName})
}
