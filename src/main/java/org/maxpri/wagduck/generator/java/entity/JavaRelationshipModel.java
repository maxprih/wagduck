package org.maxpri.wagduck.generator.java.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JavaRelationshipModel {
    private String name; // e.g., orders, userProfile
    private String type; // e.g., List<Order>, Set<Role>, UserProfile
    private String description; // JavaDoc
    private List<String> annotations; // e.g., "@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)", "@ManyToOne"
}