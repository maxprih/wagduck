package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entity_definition", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_config_id", "entityName"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class EntityDefinition {

    @Id
    @Generated
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_config_id", nullable = false)
    private ProjectConfiguration projectConfiguration;

    @Column(nullable = false)
    private String entityName;

    @Column
    private String tableName;

    @OneToMany(mappedBy = "entityDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER может быть удобен здесь
    private List<AttributeDefinition> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "sourceEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RelationshipDefinition> relationships = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}