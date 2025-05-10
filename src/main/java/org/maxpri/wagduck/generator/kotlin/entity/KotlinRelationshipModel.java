package org.maxpri.wagduck.generator.kotlin.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KotlinRelationshipModel {
    private String name; // e.g., orders, userProfile
    private String baseKotlinType; // e.g., "UserProfile", "MutableSet<Order>" (without '?' suffix for the field itself)
    private boolean isNullable; // True if the relationship field itself can be null (e.g. profile: UserProfile?)
    private String description; // KDoc
    private List<String> annotations; // e.g., "@OneToMany(mappedBy = "user")", "@ManyToOne"
    private String initializer; // e.g., "mutableSetOf()", "null"
}