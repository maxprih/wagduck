package org.maxpri.wagduck.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.maxpri.wagduck.domain.enums.BuildTool;
import org.maxpri.wagduck.domain.enums.DatabaseType;
import org.maxpri.wagduck.domain.enums.TargetFramework;
import org.maxpri.wagduck.domain.enums.TargetLanguage;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project_configuration")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ProjectConfiguration {

    @Id
    @Generated
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String projectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetFramework framework;

    @Column
    private String languageVersion;

    @Column
    private String frameworkVersion;

    @Enumerated(EnumType.STRING)
    @Column
    private BuildTool buildTool;

    @Column
    private String basePackage;

    @Column
    private String moduleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType databaseType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_option", joinColumns = @JoinColumn(name = "project_config_id"))
    @Column(name = "option_name")
    private Set<String> enabledOptions;

    @OneToMany(mappedBy = "projectConfiguration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EntityDefinition> entities = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}