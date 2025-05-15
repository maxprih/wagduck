-- liquibase formatted sql

-- changeset maxpri:task=0,init_uuid dbms:postgresql
CREATE TYPE project_language AS ENUM ('JAVA', 'KOTLIN', 'GO');
CREATE CAST (character varying as project_language) WITH INOUT AS ASSIGNMENT;
CREATE TYPE project_framework AS ENUM ('SPRING_BOOT', 'GIN');
CREATE CAST (character varying as project_framework) WITH INOUT AS ASSIGNMENT;
CREATE TYPE project_build_tool AS ENUM ('MAVEN', 'GRADLE', 'GO_MODULES');
CREATE CAST (character varying as project_build_tool) WITH INOUT AS ASSIGNMENT;
CREATE TYPE project_database_type AS ENUM ('POSTGRESQL');
CREATE CAST (character varying as project_database_type) WITH INOUT AS ASSIGNMENT;

CREATE TABLE IF NOT EXISTS project_configuration
(
    id                UUID DEFAULT uuid_generate_v7() NOT NULL,
    owner_id          uuid                            NOT NULL,
    project_name      VARCHAR(100)                    NOT NULL,
    language          project_language                NOT NULL,
    framework         project_framework               NOT NULL,
    language_version  VARCHAR(20),
    framework_version VARCHAR(20),
    build_tool        project_build_tool,
    base_package      VARCHAR(255),
    module_name       VARCHAR(255),
    database_type     project_database_type           NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_project_configuration PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_option
(
    project_config_id UUID         NOT NULL,
    option_name       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_project_option PRIMARY KEY (project_config_id, option_name)
);

CREATE TABLE IF NOT EXISTS entity_definition
(
    id                UUID DEFAULT uuid_generate_v7() NOT NULL,
    project_config_id UUID                            NOT NULL,
    entity_name       VARCHAR(100)                    NOT NULL,
    table_name        VARCHAR(100),
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_entity_definition PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS attribute_definition
(
    id                   UUID    DEFAULT uuid_generate_v7() NOT NULL,
    entity_definition_id UUID                               NOT NULL,
    attribute_name       VARCHAR(100)                       NOT NULL,
    data_type            VARCHAR(100)                       NOT NULL,
    column_name          VARCHAR(100),
    is_primary_key       BOOLEAN DEFAULT FALSE              NOT NULL,
    is_required          BOOLEAN DEFAULT FALSE              NOT NULL,
    is_unique            BOOLEAN DEFAULT FALSE              NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE,
    updated_at           TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_attribute_definition PRIMARY KEY (id)
);

CREATE TYPE relationship_type AS ENUM ('ONE_TO_ONE', 'ONE_TO_MANY', 'MANY_TO_ONE', 'MANY_TO_MANY');
CREATE CAST (character varying as relationship_type) WITH INOUT AS ASSIGNMENT;
CREATE TYPE fetch_type AS ENUM ('LAZY', 'EAGER');
CREATE CAST (character varying as fetch_type) WITH INOUT AS ASSIGNMENT;

CREATE TABLE IF NOT EXISTS relationship_definition
(
    id                            UUID       DEFAULT uuid_generate_v7() NOT NULL,
    source_entity_id              UUID                                  NOT NULL,
    source_field_name             VARCHAR(100)                          NOT NULL,
    target_entity_id              UUID                                  NOT NULL,
    relationship_type             relationship_type                     NOT NULL,
    target_field_name             VARCHAR(100),
    owning_side                   BOOLEAN    DEFAULT TRUE               NOT NULL,
    fetch_type                    fetch_type DEFAULT 'LAZY'             NOT NULL,
    join_column_name              VARCHAR(100),
    join_table_name               VARCHAR(100),
    join_table_source_column_name VARCHAR(100),
    join_table_target_column_name VARCHAR(100),
    created_at                    TIMESTAMP WITHOUT TIME ZONE,
    updated_at                    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_relationship_definition PRIMARY KEY (id)
);

CREATE TYPE api_method AS ENUM ('GET', 'GET_LIST', 'POST', 'PUT', 'DELETE');
CREATE CAST (character varying as api_method) WITH INOUT AS ASSIGNMENT;

CREATE TABLE IF NOT EXISTS api_endpoint_definition
(
    id         UUID DEFAULT uuid_generate_v7() NOT NULL,
    entity_id  UUID                            NOT NULL,
    http_path  VARCHAR(255)                    NOT NULL,
    api_method api_method                      NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_api_endpoint_definition PRIMARY KEY (id)
);

ALTER TABLE entity_definition
    ADD CONSTRAINT uq_entity_definition_project_name UNIQUE (project_config_id, entity_name);

ALTER TABLE attribute_definition
    ADD CONSTRAINT uq_attribute_entity_name UNIQUE (entity_definition_id, attribute_name);

ALTER TABLE api_endpoint_definition
    ADD CONSTRAINT uq_api_endpoint_project_path_method UNIQUE (entity_id, http_path, api_method);

ALTER TABLE project_option
    ADD CONSTRAINT fk_project_options_on_project_config FOREIGN KEY (project_config_id) REFERENCES project_configuration (id) ON DELETE CASCADE;

ALTER TABLE entity_definition
    ADD CONSTRAINT fk_entity_definition_on_project_config FOREIGN KEY (project_config_id) REFERENCES project_configuration (id) ON DELETE CASCADE;

ALTER TABLE attribute_definition
    ADD CONSTRAINT fk_attribute_definition_on_entity FOREIGN KEY (entity_definition_id) REFERENCES entity_definition (id) ON DELETE CASCADE;

ALTER TABLE relationship_definition
    ADD CONSTRAINT fk_relationship_definition_on_source_entity FOREIGN KEY (source_entity_id) REFERENCES entity_definition (id) ON DELETE CASCADE;

ALTER TABLE api_endpoint_definition
    ADD CONSTRAINT fk_api_endpoint_definition_on_entity FOREIGN KEY (entity_id) REFERENCES entity_definition (id) ON DELETE CASCADE;

CREATE INDEX idx_project_configuration_owner ON project_configuration (owner_id);
CREATE INDEX idx_entity_definition_project ON entity_definition (project_config_id);
CREATE INDEX idx_attribute_definition_entity ON attribute_definition (entity_definition_id);
CREATE INDEX idx_relationship_definition_source ON relationship_definition (source_entity_id);
CREATE INDEX idx_api_endpoint_definition_project ON api_endpoint_definition (entity_id);
CREATE INDEX idx_project_options_project ON project_option (project_config_id);
