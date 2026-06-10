package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIProfessionalRankingResponse;
import com.iclinical.technology.bi.dto.BISpecialtyRankingResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Repository
public class BIRankingRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIRankingRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BISpecialtyRankingResponse> specialties(BIFilter filter) {
        var sql = """
            select s.id as specialty_id,
                   s.name as specialty_name,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as total_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'completed') as completed_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'cancelled') as cancelled_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'no_show') as no_show_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status in ('scheduled', 'confirmed', 'completed', 'no_show')) as occupied_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as operative_appointments
            from specialties s
            join professional_specialties ps on ps.specialty_id = s.id and ps.status = 'active'
            join professionals p on p.id = ps.professional_id and p.status = 'active'
            left join appointments a on a.professional_id = p.id
                and a.start_at >= :from
                and a.start_at <= :to
            where s.status = 'active'
            group by s.id, s.name
            order by total_appointments desc, specialty_name asc
            limit 10
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> {
            var occupied = longValue(rs, "occupied_appointments");
            var operative = longValue(rs, "operative_appointments");
            return new BISpecialtyRankingResponse(
                rs.getObject("specialty_id", UUID.class),
                rs.getString("specialty_name"),
                longValue(rs, "total_appointments"),
                longValue(rs, "completed_appointments"),
                longValue(rs, "cancelled_appointments"),
                longValue(rs, "no_show_appointments"),
                rate(occupied, operative),
                true
            );
        });
    }

    public List<BIProfessionalRankingResponse> professionals(BIFilter filter) {
        var sql = """
            select p.id as professional_id,
                   concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   coalesce(chosen_specialty.specialty_name, 'Sin especialidad') as specialty_name,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted') as total_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'cancelled') as cancelled_appointments,
                   count(distinct a.id) filter (where a.appointment_type <> 'blocked_slot' and a.status = 'no_show') as no_show_appointments,
                   count(distinct a.id) filter (where a.is_overbooking = true and a.status <> 'deleted') as overbookings,
                   count(distinct cr.id) filter (where cr.status <> 'deleted') as clinical_records_total
            from professionals p
            left join lateral (
                select s.name as specialty_name
                from professional_specialties ps
                join specialties s on s.id = ps.specialty_id and s.status = 'active'
                where ps.professional_id = p.id
                  and ps.status = 'active'
                order by ps.is_primary desc, ps.created_at asc
                limit 1
            ) chosen_specialty on true
            left join appointments a on a.professional_id = p.id
                and a.start_at >= :from
                and a.start_at <= :to
            left join clinical_records cr on cr.professional_id = p.id
                and cr.record_date >= :from
                and cr.record_date <= :to
            where p.status = 'active'
              and (cast(:specialtyId as uuid) is null or exists (
                  select 1 from professional_specialties ps_filter
                  where ps_filter.professional_id = p.id
                    and ps_filter.specialty_id = cast(:specialtyId as uuid)
                    and ps_filter.status = 'active'
              ))
            group by p.id, p.first_name, p.last_name, chosen_specialty.specialty_name
            order by total_appointments desc, clinical_records_total desc, professional_name asc
            limit 10
            """;
        return jdbcTemplate.query(sql, params(filter), (rs, rowNum) -> new BIProfessionalRankingResponse(
            rs.getObject("professional_id", UUID.class),
            rs.getString("professional_name"),
            rs.getString("specialty_name"),
            longValue(rs, "total_appointments"),
            longValue(rs, "cancelled_appointments"),
            longValue(rs, "no_show_appointments"),
            longValue(rs, "overbookings"),
            longValue(rs, "clinical_records_total")
        ));
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0
            ? BigDecimal.ZERO.setScale(2)
            : BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
