package com.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SafeSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        log.info("Running safe schema migration to patch NULL values...");
        try {
            // Patch balance
            jdbcTemplate.execute("UPDATE users SET balance = 0 WHERE balance IS NULL");
            // Patch created_at
            jdbcTemplate.execute("UPDATE users SET created_at = NOW() WHERE created_at IS NULL");
            log.info("Database patch successful.");
        } catch (Exception e) {
            log.warn("Schema migration failed (might be expected if table doesn't exist yet): {}", e.getMessage());
        }
    }
}
