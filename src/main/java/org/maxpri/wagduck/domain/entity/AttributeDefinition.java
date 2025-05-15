package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attribute_definition", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"entity_definition_id", "attribute_name"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class AttributeDefinition {

    @Id
    @Generated
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_definition_id", nullable = false)
    private EntityDefinition entityDefinition;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String dataType;

    @Column
    private String columnName;

    @Column(name = "is_primary_key", nullable = false)
    private boolean isPrimaryKey;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_unique", nullable = false)
    private boolean isUnique;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}