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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private EntityDefinition sourceEntity;

    @Column(nullable = false)
    private String sourceFieldName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private EntityDefinition targetEntity;

    @Column
    private String targetFieldName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType;

    @Column(nullable = false)
    private boolean owningSide = true;

    @Enumerated(EnumType.STRING)
    private FetchType fetchType = FetchType.LAZY;

    @Column
    private String joinColumnName;

    @Column
    private String joinTableName;

    @Column
    private String joinTableSourceColumnName;

    @Column
    private String joinTableTargetColumnName;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}