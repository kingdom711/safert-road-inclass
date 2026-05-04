-- V12: Team / TeamMember domain (B1)
-- Note: Flyway is currently disabled (spring.flyway.enabled=false) and JPA
-- runs with ddl-auto=update, so these tables are auto-created from entities.
-- This script documents the intended schema and serves as a reference for
-- future Flyway adoption / manual SQLite recovery.

CREATE TABLE IF NOT EXISTS teams (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    leader_user_id INTEGER NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uk_teams_site_normalized_name UNIQUE (site_name, normalized_name),
    CONSTRAINT fk_teams_leader_user FOREIGN KEY (leader_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS team_members (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at DATETIME,
    approved_at DATETIME,
    approved_by INTEGER,
    left_at DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_team_members_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_team_members_user_status ON team_members(user_id, status);
CREATE INDEX IF NOT EXISTS idx_team_members_team_status ON team_members(team_id, status);

-- Enforces "한 사용자당 ACTIVE 멤버십 1개" (SQLite partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS uk_team_members_user_active
    ON team_members(user_id)
    WHERE status = 'ACTIVE';

-- Add team FK on users
ALTER TABLE users ADD COLUMN team_id INTEGER REFERENCES teams(id);
CREATE INDEX IF NOT EXISTS idx_users_team_id ON users(team_id);
