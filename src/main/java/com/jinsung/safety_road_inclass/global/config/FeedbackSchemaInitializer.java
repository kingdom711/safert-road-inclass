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
public class FeedbackSchemaInitializer implements ApplicationRunner {

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
                CREATE TABLE IF NOT EXISTS feedback_posts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at DATETIME,
                    updated_at DATETIME,
                    author_id INTEGER NOT NULL,
                    category VARCHAR(30) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    title VARCHAR(120) NOT NULL,
                    content TEXT NOT NULL,
                    page_path VARCHAR(120),
                    attachment_url VARCHAR(500),
                    admin_reply TEXT,
                    replied_at DATETIME,
                    replied_by_id INTEGER
                )
                """);
        addColumnIfMissing(connection, "feedback_posts", "created_at", "ALTER TABLE feedback_posts ADD COLUMN created_at DATETIME");
        addColumnIfMissing(connection, "feedback_posts", "updated_at", "ALTER TABLE feedback_posts ADD COLUMN updated_at DATETIME");
        addColumnIfMissing(connection, "feedback_posts", "author_id", "ALTER TABLE feedback_posts ADD COLUMN author_id INTEGER");
        addColumnIfMissing(connection, "feedback_posts", "category", "ALTER TABLE feedback_posts ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'OTHER'");
        addColumnIfMissing(connection, "feedback_posts", "status", "ALTER TABLE feedback_posts ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'OPEN'");
        addColumnIfMissing(connection, "feedback_posts", "title", "ALTER TABLE feedback_posts ADD COLUMN title VARCHAR(120) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "feedback_posts", "content", "ALTER TABLE feedback_posts ADD COLUMN content TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "feedback_posts", "page_path", "ALTER TABLE feedback_posts ADD COLUMN page_path VARCHAR(120)");
        addColumnIfMissing(connection, "feedback_posts", "attachment_url", "ALTER TABLE feedback_posts ADD COLUMN attachment_url VARCHAR(500)");
        addColumnIfMissing(connection, "feedback_posts", "admin_reply", "ALTER TABLE feedback_posts ADD COLUMN admin_reply TEXT");
        addColumnIfMissing(connection, "feedback_posts", "replied_at", "ALTER TABLE feedback_posts ADD COLUMN replied_at DATETIME");
        addColumnIfMissing(connection, "feedback_posts", "replied_by_id", "ALTER TABLE feedback_posts ADD COLUMN replied_by_id INTEGER");
        createCommonIndexes();
    }

    private void initializePostgres(Connection connection) throws SQLException {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS feedback_posts (
                    id BIGSERIAL PRIMARY KEY,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP,
                    author_id BIGINT NOT NULL,
                    category VARCHAR(30) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    title VARCHAR(120) NOT NULL,
                    content TEXT NOT NULL,
                    page_path VARCHAR(120),
                    attachment_url VARCHAR(500),
                    admin_reply TEXT,
                    replied_at TIMESTAMP,
                    replied_by_id BIGINT
                )
                """);
        addColumnIfMissing(connection, "feedback_posts", "created_at", "ALTER TABLE feedback_posts ADD COLUMN created_at TIMESTAMP");
        addColumnIfMissing(connection, "feedback_posts", "updated_at", "ALTER TABLE feedback_posts ADD COLUMN updated_at TIMESTAMP");
        addColumnIfMissing(connection, "feedback_posts", "author_id", "ALTER TABLE feedback_posts ADD COLUMN author_id BIGINT");
        addColumnIfMissing(connection, "feedback_posts", "category", "ALTER TABLE feedback_posts ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'OTHER'");
        addColumnIfMissing(connection, "feedback_posts", "status", "ALTER TABLE feedback_posts ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'OPEN'");
        addColumnIfMissing(connection, "feedback_posts", "title", "ALTER TABLE feedback_posts ADD COLUMN title VARCHAR(120) NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "feedback_posts", "content", "ALTER TABLE feedback_posts ADD COLUMN content TEXT NOT NULL DEFAULT ''");
        addColumnIfMissing(connection, "feedback_posts", "page_path", "ALTER TABLE feedback_posts ADD COLUMN page_path VARCHAR(120)");
        addColumnIfMissing(connection, "feedback_posts", "attachment_url", "ALTER TABLE feedback_posts ADD COLUMN attachment_url VARCHAR(500)");
        addColumnIfMissing(connection, "feedback_posts", "admin_reply", "ALTER TABLE feedback_posts ADD COLUMN admin_reply TEXT");
        addColumnIfMissing(connection, "feedback_posts", "replied_at", "ALTER TABLE feedback_posts ADD COLUMN replied_at TIMESTAMP");
        addColumnIfMissing(connection, "feedback_posts", "replied_by_id", "ALTER TABLE feedback_posts ADD COLUMN replied_by_id BIGINT");
        createCommonIndexes();
    }

    private void createCommonIndexes() {
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_feedback_author ON feedback_posts(author_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_feedback_status ON feedback_posts(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_feedback_category ON feedback_posts(category)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON feedback_posts(created_at)");
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
