package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIFHIRKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BIFHIRRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIFHIRRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIFHIRKpiResponse load(BIFilter filter) {
        var sql = """
            select
                0 as fhir_bundles_generated,
                (
                    (select count(*) from patients p where p.status <> 'deleted') +
                    (select count(*) from professionals pr where pr.status <> 'deleted') +
                    (select count(*) from clinical_records cr where cr.status <> 'deleted' and cr.record_date >= :from and cr.record_date <= :to) +
                    (select count(*) from clinical_diagnoses cd where cd.status = 'active' and cd.created_at >= :from and cd.created_at <= :to) +
                    (select count(*) from clinical_prescriptions cp where cp.status = 'active' and cp.created_at >= :from and cp.created_at <= :to) +
                    (select count(*) from clinical_documents d where d.status = 'active' and d.created_at >= :from and d.created_at <= :to)
                ) as fhir_resources_available
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> new BIFHIRKpiResponse(
            longValue(rs, "fhir_bundles_generated"),
            longValue(rs, "fhir_resources_available")
        ));
    }
}
