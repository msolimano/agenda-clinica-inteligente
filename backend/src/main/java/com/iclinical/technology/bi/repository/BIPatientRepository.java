package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIPatientKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BIPatientRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIPatientRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIPatientKpiResponse load(BIFilter filter) {
        var sql = """
            select
                count(*) filter (where p.status <> 'deleted') as total_patients,
                count(*) filter (where p.status = 'active') as active_patients,
                count(*) filter (where p.status <> 'deleted' and p.created_at >= :from and p.created_at <= :to) as new_patients_in_range
            from patients p
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> new BIPatientKpiResponse(
            longValue(rs, "total_patients"),
            longValue(rs, "active_patients"),
            longValue(rs, "new_patients_in_range")
        ));
    }
}
