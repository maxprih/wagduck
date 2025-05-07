package org.maxpri.wagduck.generator.model;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public record GeneratedFileResult(String filename, byte[] contentBytes) {
    public Resource resource() {
        return new ByteArrayResource(contentBytes);
    }
}