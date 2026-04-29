package com.jinsung.safety_road_inclass.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase();
            if (product.contains("sqlite")) {
                initializeSqlite(connection);
            } else if (product.contains("postgres")) {
                initializePostgres(connection);
            }
        }
    }

    private void initializeSqlite(Connection connection) throws SQLException {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at DATETIME,
                    updated_at DATETIME,
                    name VARCHAR(100) NOT NULL,
                    normalized_name VARCHAR(100) NOT NULL,
                    site_name VARCHAR(100) NOT NULL,
                    leader_user_id INTEGER NOT NULL,
                    CONSTRAINT uk_teams_site_normalized_name UNIQUE (site_name, normalized_name)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS team_members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at DATETIME,
                    updated_at DATETIME,
                    approved_at DATETIME,
                    joined_at DATETIME,
                    left_at DATETIME,
                    status VARCHAR(20) NOT NULL,
                    approved_by INTEGER,
                    team_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id)
                )
                """);
        addColumnIfMissing(connection, "users", "team_id", "ALTER TABLE users ADD COLUMN team_id INTEGER");
        createCommonIndexes();
    }

    private void initializePostgres(Connection connection) throws SQLException {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    id BIGSERIAL PRIMARY KEY,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP,
                    name VARCHAR(100) NOT NULL,
                    normalized_name VARCHAR(100) NOT NULL,
                    site_name VARCHAR(100) NOT NULL,
                    leader_user_id BIGINT NOT NULL,
                    CONSTRAINT uk_teams_site_normalized_name UNIQUE (site_name, normalized_name)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS team_members (
                    id BIGSERIAL PRIMARY KEY,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP,
                    approved_at TIMESTAMP,
                    joined_at TIMESTAMP,
                    left_at TIMESTAMP,
                    status VARCHAR(20) NOT NULL,
                    approved_by BIGINT,
                    team_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id)
                )
                """);
        addColumnIfMissing(connection, "users", "team_id", "ALTER TABLE users ADD COLUMN team_id BIGINT");
        createCommonIndexes();
    }

    private void createCommonIndexes() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_team_id ON users(team_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_team_members_user_status ON team_members(user_id, status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_team_members_team_status ON team_members(team_id, status)");
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_team_members_user_active
                ON team_members(user_id)
                WHERE status = 'ACTIVE'
                """);
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String ddl) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, table, column)) {
            if (!rs.next()) {
                jdbcTemplate.execute(ddl);
                log.info("Schema patched: {}.{} added", table, column);
            }
        }
    }
}
