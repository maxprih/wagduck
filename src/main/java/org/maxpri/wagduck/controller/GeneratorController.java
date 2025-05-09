package org.maxpri.wagduck.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maxpri.wagduck.generator.GenerationService;
import org.maxpri.wagduck.generator.model.GeneratedFileResult;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/project/{projectId}/generate")
@RequiredArgsConstructor
@Slf4j
public class GeneratorController {

    private final GenerationService generationService;

    @PostMapping
    public ResponseEntity<Resource> generateProject(
            @PathVariable UUID projectId, @AuthenticationPrincipal Jwt jwt) {

        GeneratedFileResult result = generationService.generateProject(projectId, UUID.fromString(jwt.getSubject()));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.filename());
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        log.info("Successfully generated file '{}'. Sending response.", result.filename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(result.contentBytes().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Generic file download
                .body(result.resource());
    }
}