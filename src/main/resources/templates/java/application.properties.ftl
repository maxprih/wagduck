server.port=${serverPort}

spring.application.name=${applicationName}
<#if dbUrl?? && dbUrl?has_content>

spring.datasource.url=${dbUrl}
spring.datasource.username=${dbUsername}
spring.datasource.password=${dbPassword}

spring.jpa.hibernate.ddl-auto=${jpaDdlAuto!'update'}
spring.jpa.show-sql=${jpaShowSql?string('true', 'false')}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database=${jpaDatabasePlatform?lower_case}
</#if>

logging.level.org.springframework=INFO
logging.level.${basePackage}=DEBUG
logging.level.org.hibernate.SQL=${jpaShowSql?string('DEBUG', 'INFO')}
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html