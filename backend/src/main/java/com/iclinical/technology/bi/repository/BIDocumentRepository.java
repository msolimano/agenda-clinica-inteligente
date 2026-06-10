package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIBarItemResponse;
import com.iclinical.technology.bi.dto.BIDocumentKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BIDocumentRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIDocumentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIDocumentKpiResponse load(BIFilter filter) {
        var totalSql = """
            select
                count(*) filter (where d.status = 'active') as clinical_documents_total,
                count(*) filter (where d.status = 'deleted') as deleted_documents
            from clinical_documents d
            where d.created_at >= :from
              and d.created_at <= :to
            """;
        var totals = jdbcTemplate.queryForObject(totalSql, params(filter), (rs, rowNum) -> new long[] {
            longValue(rs, "clinical_documents_total"),
            longValue(rs, "deleted_documents")
        });
        return new BIDocumentKpiResponse(totals[0], documentsByType(filter), totals[1]);
    }

    private List<BIBarItemResponse> documentsByType(BIFilter filter) {
        var sql = """
            select coalesce(nullif(d.document_type, ''), 'Sin tipo') as label,
                   count(*) as value
            from clinical_documents d
            where d.status = 'active'
              and d.created_at >= :from
              and d.created_at <= :to
            group by label
            order by value desc, label asc
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> new BIBarItemResponse(rs.getString("label"), longValue(rs, "value")));
    }
}
