package com.payment.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        log.info("Checking database schema for missing columns...");
        try {
            // Check 'users' table
            addColumnIfMissing("users", "password", "VARCHAR(255)");
            
            // Check 'transactions' table
            addColumnIfMissing("transactions", "type", "VARCHAR(50)");
            addColumnIfMissing("transactions", "status", "VARCHAR(50)");
            addColumnIfMissing("transactions", "reference_id", "VARCHAR(255) UNIQUE");
            addColumnIfMissing("transactions", "fraud_flag", "BOOLEAN DEFAULT FALSE");
            
            log.info("Schema migration check completed.");
        } catch (Exception e) {
            log.error("Failed to run schema migration: {}", e.getMessage());
        }
    }

    private void addColumnIfMissing(String tableName, String columnName, String columnType) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM information_schema.columns WHERE table_name=? AND column_name=?",
            Integer.class,
            tableName,
            columnName.toLowerCase()
        );

        if (count == null || count == 0) {
            log.info("Column '{}' missing in '{}' table. Adding it...", columnName, tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        }
    }
}
