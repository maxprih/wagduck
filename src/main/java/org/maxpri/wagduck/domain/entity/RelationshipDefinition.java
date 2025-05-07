package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.maxpri.wagduck.domain.enums.RelationshipType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "relationship_definition")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class RelationshipDefinition {

    @Id
    @Generated
    private UUID id;

    // Сущность, ИЗ которой идет связь (владелец поля)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private EntityDefinition sourceEntity;

    // Имя поля в исходной сущности (e.g., "orders" в User)
    @Column(nullable = false)
    private String sourceFieldName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private EntityDefinition targetEntity;

    // Имя поля в целевой сущности для обратной связи (e.g., "user" в Order). null если однонаправленная.
    @Column
    private String targetFieldName; // (mappedBy в JPA)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType; // Enum: ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY

    // Кто владеет связью? Важно для JPA (где @JoinColumn / @JoinTable)
    // true - sourceEntity владеет (JoinColumn/Table здесь), false - targetEntity владеет
    @Column(nullable = false)
    private boolean owningSide = true; // Обычно true для *ToOne, false для OneToMany без mappedBy, true для одного из ManyToMany

    @Enumerated(EnumType.STRING)
    private FetchType fetchType = FetchType.LAZY; // По умолчанию LAZY

    // --- Настройки для Join (если нужно переопределить стандартные) ---
    @Column
    private String joinColumnName; // Для *ToOne или инверсной стороны OneToMany

    @Column
    private String joinTableName; // Для ManyToMany

    @Column
    private String joinTableSourceColumnName; // Для ManyToMany

    @Column
    private String joinTableTargetColumnName; // Для ManyToMany

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}