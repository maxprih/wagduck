package ${packageName}

<#assign allImports = []>
<#if imports?has_content>
    <#assign allImports = allImports + imports>
</#if>
<#assign uniqueImports = allImports?sort>
<#if uniqueImports?has_content>
import (
    <#list uniqueImports as imp>
    "${imp}"
    </#list>
)
</#if>

<#if description?has_content>
// ${description}
<#else>
// ${structName} implements ${interfaceName} using GORM.
</#if>
type ${structName} struct {
    db *gorm.DB
}

// New${structName?keep_before("Repository")?cap_first}Repository creates a new instance of ${structName}.
func New${structName?keep_before("Repository")?cap_first}Repository(db *gorm.DB) ${interfaceName} {
    return &${structName}{db: db}
}

<#list methods as method>
<#-- Method signature -->
func (${receiverName} *${structName}) ${method.name}(<#list method.parameters as param>${param.name} ${param.type}<#if param?has_next>, </#if></#list>) (<#list method.returnTypes as rt>${rt.type}<#if rt?has_next>, </#if></#list>) {
<#-- Basic GORM implementation based on common CRUD method names -->
<#-- This is a simplified example; your Java mapper would generate more precise GORM calls -->
<#assign entityVarName = entityStructName?uncap_first>
<#assign modelPkgPrefix = entityPackageName?split("/")?last + "."> <#-- e.g. model. -->
    <#if method.name == "Create" || method.name == "Create${entityName}">
        <#assign inputParam = method.parameters?filter(p -> p.type?starts_with("*" + modelPkgPrefix + entityStructName))?first>
    if err := ${receiverName}.db.WithContext(${method.parameters?first.name}).Create(${inputParam.name}).Error; err != nil {
        return nil, err
    }
    return ${inputParam.name}, nil
    <#elseif method.name == "GetByID" || method.name == "Get${entityName}ByID">
        <#assign idParam = method.parameters?filter(p -> p.name == entityIdParameterName)?first>
    var ${entityVarName} ${modelPkgPrefix}${entityStructName}
    if err := ${receiverName}.db.WithContext(${method.parameters?first.name}).First(&${entityVarName}, "${entityIdStructField} = ?", ${idParam.name}).Error; err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return nil, nil // Or a custom not found error
        }
        return nil, err
    }
    return &${entityVarName}, nil
    <#elseif method.name == "Update" || method.name == "Update${entityName}">
        <#assign inputParam = method.parameters?filter(p -> p.type?starts_with("*" + modelPkgPrefix + entityStructName))?first>
        <#-- Assuming the inputParam has the ID set for the WHERE clause -->
    if err := ${receiverName}.db.WithContext(${method.parameters?first.name}).Save(${inputParam.name}).Error; err != nil {
        return nil, err
    }
    return ${inputParam.name}, nil
    <#elseif method.name == "Delete" || method.name == "Delete${entityName}">
        <#assign idParam = method.parameters?filter(p -> p.name == entityIdParameterName)?first>
    if err := ${receiverName}.db.WithContext(${method.parameters?first.name}).Delete(&${modelPkgPrefix}${entityStructName}{}, "${entityIdStructField} = ?", ${idParam.name}).Error; err != nil {
        return err
    }
    return nil
    <#elseif method.name == "List" || method.name == "List${entityName}s">
    var ${entityVarName}s []${modelPkgPrefix}${entityStructName}
    // Add pagination, filtering, sorting parameters to method if needed
    if err := ${receiverName}.db.WithContext(${method.parameters?first.name}).Find(&${entityVarName}s).Error; err != nil {
        return nil, err
    }
    return ${entityVarName}s, nil
    <#else>
    // TODO: Implement GORM logic for ${method.name}
    panic("not implemented") <#-- Default for unhandled methods -->
    </#if>
}
</#list>