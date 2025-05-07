package org.maxpri.wagduck.domain.enums;

/**
 * Supported database types for generated projects.
 */
public enum DatabaseType {
    POSTGRESQL,
    MYSQL,
    MARIADB,
    ORACLE, // Depending on Spring Data support
    SQL_SERVER, // Depending on Spring Data support
    MONGODB,    // NoSQL example
    H2,         // For testing/dev
    NONE        // No database integration
}