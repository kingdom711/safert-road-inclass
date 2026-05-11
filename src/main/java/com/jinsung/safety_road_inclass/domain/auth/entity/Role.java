package com.jinsung.safety_road_inclass.domain.auth.entity;

/**
 * Account-level roles used by Spring Security.
 *
 * Game participation roles are stored separately in the game profile/local role
 * selection flow. Keep project administration out of those participant roles.
 */
public enum Role {

    ROLE_WORKER,
    ROLE_SUPERVISOR,
    ROLE_SAFETY_MANAGER,
    ROLE_ADMIN,
    ROLE_PROJECT_ADMIN;

    public String getSimpleName() {
        return this.name().replace("ROLE_", "");
    }
}
