package org.maxpri.wagduck.generator.java.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.maxpri.wagduck.domain.entity.ProjectConfiguration;
import org.maxpri.wagduck.domain.enums.ProjectOptions;
import org.maxpri.wagduck.util.NamingUtils;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {NamingUtils.class})
public interface JavaApplicationMapper {

    @Mapping(target = "packageName", expression = "java(config.getBasePackage())")
    @Mapping(target = "className", expression = "java(deriveApplicationClassName(config.getModuleName()))")
    @Mapping(target = "imports", expression = "java(determineAppImports(config))")
    @Mapping(target = "annotations", expression = "java(determineAppAnnotations(config))")
    JavaApplicationModel toJavaApplicationModel(ProjectConfiguration config);

    default String deriveApplicationClassName(String moduleName) {
        if (moduleName == null || moduleName.isEmpty()) {
            return "Application";
        }
        String namePart = NamingUtils.toPascalCase(moduleName);
        return namePart + "Application";
    }

    default Set<String> determineAppImports(ProjectConfiguration config) {
        Set<String> imports = new java.util.HashSet<>();
         imports.add("org.springframework.boot.autoconfigure.SpringBootApplication");
         imports.add("org.springframework.boot.SpringApplication");

         if (NamingUtils.checkProjectOption(config, ProjectOptions.ENABLE_JPA_AUDITING)) {
             imports.add("org.springframework.data.jpa.repository.config.EnableJpaAuditing");
         }
         return imports;
    }

     default List<String> determineAppAnnotations(ProjectConfiguration config) {
         List<String> annotations = new java.util.ArrayList<>();
         annotations.add("@SpringBootApplication");

         if (NamingUtils.checkProjectOption(config, ProjectOptions.ENABLE_JPA_AUDITING)) {
             annotations.add("@EnableJpaAuditing");
         }
         return annotations;
     }
}