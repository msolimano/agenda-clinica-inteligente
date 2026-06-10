package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIAIKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BIAIRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIAIRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIAIKpiResponse load(BIFilter filter) {
        var sql = """
            select
                (select count(*) from ai_analyses a where a.status <> 'deleted' and a.created_at >= :from and a.created_at <= :to) as ai_analyses_total,
                (select count(*) from ai_analyses a where a.status = 'completed' and a.created_at >= :from and a.created_at <= :to) as ai_analyses_completed,
                (select count(*) from ai_analyses a where a.status = 'failed' and a.created_at >= :from and a.created_at <= :to) as ai_analyses_failed,
                (select count(*) from consents c where c.consent_type = 'ai_analysis' and c.granted = true and c.status = 'active') as ai_consents_active
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> new BIAIKpiResponse(
            longValue(rs, "ai_analyses_total"),
            longValue(rs, "ai_analyses_completed"),
            longValue(rs, "ai_analyses_failed"),
            longValue(rs, "ai_consents_active")
        ));
    }
}
