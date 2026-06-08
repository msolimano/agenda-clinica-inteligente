package com.iclinical.technology.professionals;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class DefaultOrganizationProvider {

    private static final UUID DEFAULT_ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final JdbcTemplate jdbcTemplate;

    DefaultOrganizationProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    UUID resolve(UUID requestedOrganizationId) {
        if (requestedOrganizationId != null) {
            return requestedOrganizationId;
        }

        jdbcTemplate.update("""
            insert into organizations (id, name, legal_name, identifier, status)
            values (?, ?, ?, ?, 'active')
            on conflict (id) do nothing
            """, DEFAULT_ORGANIZATION_ID, "I-Clinical Demo", "I-Clinical Technology", "ICLINICAL-DEMO");

        return DEFAULT_ORGANIZATION_ID;
    }
}
