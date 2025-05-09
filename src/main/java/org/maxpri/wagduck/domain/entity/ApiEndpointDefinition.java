package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.maxpri.wagduck.domain.enums.HttpMethod;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "api_endpoint_definition", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_config_id", "httpPath", "httpMethod"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ApiEndpointDefinition {

    @Id
    @Generated
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_config_id", nullable = false)
    private ProjectConfiguration projectConfiguration;

    @Column(nullable = false)
    private String httpPath; // e.g., "/users/{id}", "/orders"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HttpMethod httpMethod; // Enum: GET, POST, PUT, DELETE, PATCH, etc.

    @Column(length = 1000)
    private String summary; // Краткое описание (для Swagger/документации)

    @Column(length = 4000)
    private String description; // Полное описание

    // Параметры этого эндпоинта (path, query, header)
    @OneToMany(mappedBy = "apiEndpoint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApiParameterDefinition> parameters = new ArrayList<>();

    // Описание тела запроса (e.g., ссылка на DTO/JSON Schema, или просто описание)
    @Column(length = 4000)
    private String requestBodyDescription;
    @Column // MIME-тип запроса (e.g., "application/json")
    private String requestBodyContentType;
    private boolean requestBodyRequired = false;

    // Описание ответа (e.g., ссылка на DTO/JSON Schema, или просто описание)
    // Может быть несколько ответов на разные коды статуса - усложнение! Пока один основной.
    @Column(length = 4000)
    private String responseBodyDescription;
    @Column // MIME-тип ответа (e.g., "application/json")
    private String responseBodyContentType;
    @Column // Основной успешный HTTP статус (e.g., 200, 201, 204)
    private Integer successStatusCode = 200;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}