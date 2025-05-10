package ${packageName}

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.boot.runApplication

@SpringBootApplication
<#if enableAuditing>
@EnableJpaAuditing
</#if>
class ${moduleName}Application

fun main(args: Array<String>) {
	runApplication<${moduleName}Application>(*args)
}
