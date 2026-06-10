package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIEfficiencyMetricResponse;
import com.iclinical.technology.bi.dto.BIEfficiencyResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
public class BIEfficiencyRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIEfficiencyRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIEfficiencyResponse load(BIFilter filter) {
        var sql = """
            select
                (select count(*) from appointments a where a.appointment_type <> 'blocked_slot' and a.status <> 'deleted' and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as operative_appointments,
                (select count(*) from appointments a where a.appointment_type <> 'blocked_slot' and a.status = 'cancelled' and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as cancelled_appointments,
                (select count(*) from appointments a where a.appointment_type <> 'blocked_slot' and a.status = 'no_show' and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as no_show_appointments,
                (select count(*) from appointments a where a.appointment_type <> 'blocked_slot' and a.status = 'confirmed' and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as confirmed_appointments,
                (select count(*) from appointments a where a.is_overbooking = true and a.status <> 'deleted' and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as overbookings,
                (select count(*) from appointments a where a.appointment_type <> 'blocked_slot' and a.status in ('scheduled', 'confirmed', 'completed', 'no_show') and a.start_at >= :from and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as occupied_appointments,
                (select count(*) from clinical_records cr where cr.status <> 'deleted' and cr.record_date >= :from and cr.record_date <= :to
            """ + professionalAndSpecialtyFilter("cr") + """
                ) as clinical_records_total,
                (select count(*) from clinical_records cr where cr.status = 'closed' and cr.record_date >= :from and cr.record_date <= :to
            """ + professionalAndSpecialtyFilter("cr") + """
                ) as clinical_records_closed
            """;
        return jdbcTemplate.queryForObject(sql, params(filter), (rs, rowNum) -> {
            var operative = longValue(rs, "operative_appointments");
            var clinicalTotal = longValue(rs, "clinical_records_total");
            return new BIEfficiencyResponse(
                filter.from(),
                filter.to(),
                List.of(
                    metric("occupancyRate", "Ocupacion aproximada", longValue(rs, "occupied_appointments"), operative, true, "Citas ocupadas sobre citas operativas."),
                    metric("cancellationRate", "Tasa de cancelacion", longValue(rs, "cancelled_appointments"), operative, false, "Cancelaciones sobre citas operativas."),
                    metric("noShowRate", "Tasa no show", longValue(rs, "no_show_appointments"), operative, false, "Inasistencias sobre citas operativas."),
                    metric("confirmationRate", "Tasa de confirmacion", longValue(rs, "confirmed_appointments"), operative, false, "Citas confirmadas sobre citas operativas."),
                    metric("overbookingRate", "Tasa de sobrecupos", longValue(rs, "overbookings"), operative, false, "Sobrecupos sobre citas operativas."),
                    metric("clinicalClosureRate", "Cierre de fichas", longValue(rs, "clinical_records_closed"), clinicalTotal, false, "Fichas cerradas sobre fichas creadas.")
                )
            );
        });
    }

    private BIEfficiencyMetricResponse metric(String key, String label, long numerator, long denominator, boolean approximate, String description) {
        return new BIEfficiencyMetricResponse(key, label, numerator, denominator, rate(numerator, denominator), approximate, description);
    }

    private BigDecimal rate(long numerator, long denominator) {
        return denominator == 0
            ? BigDecimal.ZERO.setScale(2)
            : BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
