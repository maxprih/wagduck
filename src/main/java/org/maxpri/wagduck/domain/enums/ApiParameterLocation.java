package org.maxpri.wagduck.domain.enums;

/**
 * Location of a parameter in an API request (inspired by OpenAPI).
 */
public enum ApiParameterLocation {
    PATH,
    QUERY,
    HEADER,
    COOKIE
    // BODY is typically handled separately via RequestBody DTOs
}