package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIBarItemResponse;
import com.iclinical.technology.bi.dto.BIClinicalKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BIClinicalRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIClinicalRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIClinicalKpiResponse load(BIFilter filter) {
        var totals = jdbcTemplate.queryForObject("""
            select
                (select count(*) from clinical_records cr where cr.status <> 'deleted' and cr.record_date >= :from and cr.record_date <= :to
            """ + professionalAndSpecialtyFilter("cr") + """
                ) as clinical_records_total,
                (select count(*) from clinical_records cr where cr.status = 'closed' and cr.record_date >= :from and cr.record_date <= :to
            """ + professionalAndSpecialtyFilter("cr") + """
                ) as clinical_records_closed,
                (select count(*) from clinical_evolutions ce where ce.status = 'active' and ce.evolution_date >= :from and ce.evolution_date <= :to
            """ + professionalAndSpecialtyFilter("ce") + """
                ) as evolutions_total,
                (select count(*) from clinical_diagnoses cd where cd.status = 'active' and cd.created_at >= :from and cd.created_at <= :to
            """ + professionalAndSpecialtyFilter("cd") + """
                ) as diagnoses_total,
                (select count(*) from clinical_prescriptions cp where cp.status = 'active' and cp.created_at >= :from and cp.created_at <= :to
            """ + professionalAndSpecialtyFilter("cp") + """
                ) as prescriptions_total
            """, params(filter), (rs, rowNum) -> new long[] {
                longValue(rs, "clinical_records_total"),
                longValue(rs, "clinical_records_closed"),
                longValue(rs, "evolutions_total"),
                longValue(rs, "diagnoses_total"),
                longValue(rs, "prescriptions_total")
            });
        return new BIClinicalKpiResponse(
            totals[0],
            totals[1],
            totals[2],
            totals[3],
            totals[4],
            topDiagnoses(filter),
            topMedications(filter)
        );
    }

    public List<BIBarItemResponse> topDiagnoses(BIFilter filter) {
        var sql = """
            select coalesce(nullif(cd.diagnosis_code_display, ''), nullif(cd.diagnosis_text, ''), 'Sin diagnostico') as label,
                   count(*) as value
            from clinical_diagnoses cd
            where cd.status = 'active'
              and cd.created_at >= :from
              and cd.created_at <= :to
            """ + professionalAndSpecialtyFilter("cd") + """
            group by label
            order by value desc, label asc
            limit 5
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> new BIBarItemResponse(rs.getString("label"), longValue(rs, "value")));
    }

    public List<BIBarItemResponse> topMedications(BIFilter filter) {
        var sql = """
            select coalesce(nullif(cp.medication_code_display, ''), nullif(cp.medication_name, ''), 'Sin medicamento') as label,
                   count(*) as value
            from clinical_prescriptions cp
            where cp.status = 'active'
              and cp.created_at >= :from
              and cp.created_at <= :to
            """ + professionalAndSpecialtyFilter("cp") + """
            group by label
            order by value desc, label asc
            limit 5
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> new BIBarItemResponse(rs.getString("label"), longValue(rs, "value")));
    }
}
