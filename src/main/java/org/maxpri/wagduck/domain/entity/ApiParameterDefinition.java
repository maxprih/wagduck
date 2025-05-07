package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.maxpri.wagduck.domain.enums.ApiParameterLocation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_parameter_definition", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"api_endpoint_definition_id", "parameterName", "parameterLocation"}) // Имя+Расположение уникальны для эндпоинта
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ApiParameterDefinition {

    @Id
    @Generated
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_endpoint_definition_id", nullable = false)
    private ApiEndpointDefinition apiEndpoint;

    @Column(nullable = false)
    private String parameterName; // e.g., "id", "page", "X-Request-ID"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiParameterLocation parameterLocation; // Enum: PATH, QUERY, HEADER, COOKIE

    @Column(nullable = false)
    private String dataType; // Тип данных параметра (e.g., "string", "integer", "boolean", "uuid")

    @Column(length = 1000)
    private String description;

    private boolean isRequired = false;

    @Column // Пример значения
    private String example;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}