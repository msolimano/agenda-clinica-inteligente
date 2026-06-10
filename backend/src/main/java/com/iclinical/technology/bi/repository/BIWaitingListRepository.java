package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIWaitingListKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BIWaitingListRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIWaitingListRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIWaitingListKpiResponse load(BIFilter filter) {
        var sql = """
            select
                count(*) filter (where wl.status <> 'deleted') as waiting_list_total,
                count(*) filter (where wl.status = 'scheduled') as waiting_list_scheduled,
                count(*) filter (where wl.status = 'contacted') as waiting_list_contacted,
                count(*) filter (where wl.status = 'cancelled') as waiting_list_cancelled
            from waiting_list wl
            where wl.created_at >= :from
              and wl.created_at <= :to
              and (cast(:professionalId as uuid) is null or wl.professional_id = cast(:professionalId as uuid))
              and (cast(:specialtyId as uuid) is null or wl.specialty_id = cast(:specialtyId as uuid))
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> new BIWaitingListKpiResponse(
            longValue(rs, "waiting_list_total"),
            longValue(rs, "waiting_list_scheduled"),
            longValue(rs, "waiting_list_contacted"),
            longValue(rs, "waiting_list_cancelled")
        ));
    }
}
