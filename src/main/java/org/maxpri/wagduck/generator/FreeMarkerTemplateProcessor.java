package org.maxpri.wagduck.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.StringWriter;

@Component
@RequiredArgsConstructor
public class FreeMarkerTemplateProcessor {

    private final Configuration freeMarkerConfiguration;

    @SneakyThrows
    public String process(String templateName, Object dataModel) {
        Template template = freeMarkerConfiguration.getTemplate(templateName);
        try (StringWriter writer = new StringWriter()) {
            template.process(dataModel, writer);
            return writer.toString();
        }
    }
}