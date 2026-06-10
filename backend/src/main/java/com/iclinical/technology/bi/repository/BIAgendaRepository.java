package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIAgendaKpiResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Repository
public class BIAgendaRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIAgendaRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIAgendaKpiResponse load(BIFilter filter) {
        var sql = """
            select
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as total_appointments,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'scheduled') as scheduled_appointments,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'confirmed') as confirmed_appointments,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'cancelled') as cancelled_appointments,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'no_show') as no_show_appointments,
                count(*) filter (where a.appointment_type = 'blocked_slot' and a.status = 'blocked') as blocked_slots,
                count(*) filter (where a.is_overbooking = true and a.status <> 'deleted') as overbookings,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status in ('scheduled', 'confirmed', 'completed', 'no_show')) as occupied_appointments,
                count(*) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as operative_appointments
            from appointments a
            where a.start_at >= :from
              and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a");
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> {
            var occupied = longValue(rs, "occupied_appointments");
            var operative = longValue(rs, "operative_appointments");
            var occupancyRate = operative == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(occupied).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(operative), 2, RoundingMode.HALF_UP);
            return new BIAgendaKpiResponse(
                longValue(rs, "total_appointments"),
                longValue(rs, "scheduled_appointments"),
                longValue(rs, "confirmed_appointments"),
                longValue(rs, "cancelled_appointments"),
                longValue(rs, "no_show_appointments"),
                longValue(rs, "blocked_slots"),
                longValue(rs, "overbookings"),
                occupancyRate,
                true
            );
        });
    }
}
