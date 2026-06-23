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
public class PasswordResetSchemaInitializer implements ApplicationRunner {

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
                CREATE TABLE IF NOT EXISTS password_reset_approval_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at DATETIME,
                    updated_at DATETIME,
                    user_id INTEGER NOT NULL,
                    requested_email VARCHAR(100) NOT NULL,
                    encoded_new_password VARCHAR(255) NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    processed_at DATETIME,
                    processed_by INTEGER,
                    reject_reason VARCHAR(255)
                )
                """);
        addColumnIfMissing(connection, "password_reset_approval_requests", "created_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN created_at DATETIME");
        addColumnIfMissing(connection, "password_reset_approval_requests", "updated_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN updated_at DATETIME");
        addColumnIfMissing(connection, "password_reset_approval_requests", "user_id", "ALTER TABLE password_reset_approval_requests ADD COLUMN user_id INTEGER");
        addColumnIfMissing(connection, "password_reset_approval_requests", "requested_email", "ALTER TABLE password_reset_approval_requests ADD COLUMN requested_email VARCHAR(100) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "password_reset_approval_requests", "encoded_new_password", "ALTER TABLE password_reset_approval_requests ADD COLUMN encoded_new_password VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "password_reset_approval_requests", "status", "ALTER TABLE password_reset_approval_requests ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
        addColumnIfMissing(connection, "password_reset_approval_requests", "processed_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN processed_at DATETIME");
        addColumnIfMissing(connection, "password_reset_approval_requests", "processed_by", "ALTER TABLE password_reset_approval_requests ADD COLUMN processed_by INTEGER");
        addColumnIfMissing(connection, "password_reset_approval_requests", "reject_reason", "ALTER TABLE password_reset_approval_requests ADD COLUMN reject_reason VARCHAR(255)");
        createCommonIndexes();
    }

    private void initializePostgres(Connection connection) throws SQLException {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS password_reset_approval_requests (
                    id BIGSERIAL PRIMARY KEY,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP,
                    user_id BIGINT NOT NULL,
                    requested_email VARCHAR(100) NOT NULL,
                    encoded_new_password VARCHAR(255) NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    processed_at TIMESTAMP,
                    processed_by BIGINT,
                    reject_reason VARCHAR(255)
                )
                """);
        addColumnIfMissing(connection, "password_reset_approval_requests", "created_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN created_at TIMESTAMP");
        addColumnIfMissing(connection, "password_reset_approval_requests", "updated_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN updated_at TIMESTAMP");
        addColumnIfMissing(connection, "password_reset_approval_requests", "user_id", "ALTER TABLE password_reset_approval_requests ADD COLUMN user_id BIGINT");
        addColumnIfMissing(connection, "password_reset_approval_requests", "requested_email", "ALTER TABLE password_reset_approval_requests ADD COLUMN requested_email VARCHAR(100) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "password_reset_approval_requests", "encoded_new_password", "ALTER TABLE password_reset_approval_requests ADD COLUMN encoded_new_password VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "password_reset_approval_requests", "status", "ALTER TABLE password_reset_approval_requests ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
        addColumnIfMissing(connection, "password_reset_approval_requests", "processed_at", "ALTER TABLE password_reset_approval_requests ADD COLUMN processed_at TIMESTAMP");
        addColumnIfMissing(connection, "password_reset_approval_requests", "processed_by", "ALTER TABLE password_reset_approval_requests ADD COLUMN processed_by BIGINT");
        addColumnIfMissing(connection, "password_reset_approval_requests", "reject_reason", "ALTER TABLE password_reset_approval_requests ADD COLUMN reject_reason VARCHAR(255)");
        createCommonIndexes();
    }

    private void createCommonIndexes() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_reset_approval_user ON password_reset_approval_requests(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_password_reset_approval_status ON password_reset_approval_requests(status)");
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
