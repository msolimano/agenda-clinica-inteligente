package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIAgendaKpiResponse;
import com.iclinical.technology.bi.dto.BIAppointmentTrendResponse;
import com.iclinical.technology.bi.dto.BISpecialtyOccupancyResponse;
import com.iclinical.technology.bi.dto.BITrendPointResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

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
            return new BIAgendaKpiResponse(
                longValue(rs, "total_appointments"),
                longValue(rs, "scheduled_appointments"),
                longValue(rs, "confirmed_appointments"),
                longValue(rs, "cancelled_appointments"),
                longValue(rs, "no_show_appointments"),
                longValue(rs, "blocked_slots"),
                longValue(rs, "overbookings"),
                occupancyRate(occupied, operative),
                true
            );
        });
    }

    public BIAppointmentTrendResponse appointmentTrend(BIFilter filter) {
        var sql = """
            select date(a.start_at) as appointment_date,
                   count(*) filter (where a.status = 'scheduled') as scheduled,
                   count(*) filter (where a.status = 'confirmed') as confirmed,
                   count(*) filter (where a.status = 'cancelled') as cancelled,
                   count(*) filter (where a.status = 'no_show') as no_show
            from appointments a
            where a.start_at >= :from
              and a.start_at <= :to
              and a.appointment_type <> 'blocked_slot'
              and a.status <> 'deleted'
            """ + professionalAndSpecialtyFilter("a") + """
            group by appointment_date
            order by appointment_date asc
            """;
        var valuesByDate = new LinkedHashMap<LocalDate, BITrendPointResponse>();
        jdbcTemplate.query(sql, params(filter), rs -> {
            var date = rs.getObject("appointment_date", LocalDate.class);
            valuesByDate.put(date, new BITrendPointResponse(
                date,
                longValue(rs, "scheduled"),
                longValue(rs, "confirmed"),
                longValue(rs, "cancelled"),
                longValue(rs, "no_show")
            ));
        });

        var zone = ZoneId.systemDefault();
        var current = filter.from().atZone(zone).toLocalDate();
        var end = filter.to().atZone(zone).toLocalDate();
        var points = new ArrayList<BITrendPointResponse>();
        while (!current.isAfter(end)) {
            points.add(valuesByDate.getOrDefault(current, new BITrendPointResponse(current, 0, 0, 0, 0)));
            current = current.plusDays(1);
        }
        return new BIAppointmentTrendResponse(filter.from(), filter.to(), points);
    }

    public List<BISpecialtyOccupancyResponse> specialtyOccupancy(BIFilter filter) {
        var sql = """
            select s.id as specialty_id,
                   s.name as specialty_name,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status in ('scheduled', 'confirmed', 'completed', 'no_show')) as occupied_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as operative_appointments
            from specialties s
            join professional_specialties ps on ps.specialty_id = s.id and ps.status = 'active'
            join professionals p on p.id = ps.professional_id and p.status = 'active'
            left join appointments a on a.professional_id = p.id
                and a.start_at >= :from
                and a.start_at <= :to
            where s.status = 'active'
              and (cast(:professionalId as uuid) is null or p.id = cast(:professionalId as uuid))
              and (cast(:specialtyId as uuid) is null or s.id = cast(:specialtyId as uuid))
            group by s.id, s.name
            order by operative_appointments desc, specialty_name asc
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> {
            var occupied = longValue(rs, "occupied_appointments");
            var operative = longValue(rs, "operative_appointments");
            return new BISpecialtyOccupancyResponse(
                rs.getObject("specialty_id", UUID.class),
                rs.getString("specialty_name"),
                occupied,
                operative,
                occupancyRate(occupied, operative),
                true
            );
        });
    }

    private BigDecimal occupancyRate(long occupied, long operative) {
        return operative == 0
            ? BigDecimal.ZERO.setScale(2)
            : BigDecimal.valueOf(occupied).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(operative), 2, RoundingMode.HALF_UP);
    }
}
