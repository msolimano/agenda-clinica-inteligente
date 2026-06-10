package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIAIKpiResponse;
import com.iclinical.technology.bi.dto.BIAITrendResponse;
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

    public BIAITrendResponse trends(BIFilter filter) {
        var sql = """
            select
                count(*) filter (where a.status = 'completed') as completed,
                count(*) filter (where a.status = 'failed') as failed,
                count(*) filter (where a.status = 'pending') as pending,
                count(*) filter (where a.status = 'processing') as processing
            from ai_analyses a
            where a.status <> 'deleted'
              and a.created_at >= :from
              and a.created_at <= :to
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> new BIAITrendResponse(
            longValue(rs, "completed"),
            longValue(rs, "failed"),
            longValue(rs, "pending"),
            longValue(rs, "processing")
        ));
    }
}
