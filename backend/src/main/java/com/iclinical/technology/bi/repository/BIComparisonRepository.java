package com.iclinical.technology.bi.repository;

import com.iclinical.technology.bi.BIFilter;
import com.iclinical.technology.bi.dto.BIComparisonMetricResponse;
import com.iclinical.technology.bi.dto.BIComparisonPeriodResponse;
import com.iclinical.technology.bi.dto.BIComparisonResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Repository
public class BIComparisonRepository extends BIRepositorySupport {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BIComparisonRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BIComparisonResponse load(BIFilter filter) {
        var current = loadStats(filter.from(), filter.to(), filter);
        var duration = Duration.between(filter.from(), filter.to());
        var previousTo = filter.from().minusMillis(1);
        var previousFrom = previousTo.minus(duration);
        var previous = loadStats(previousFrom, previousTo, filter);

        return new BIComparisonResponse(
            new BIComparisonPeriodResponse(filter.from(), filter.to()),
            new BIComparisonPeriodResponse(previousFrom, previousTo),
            List.of(
                metric("operativeAppointments", "Citas operativas", current.operativeAppointments(), previous.operativeAppointments()),
                metric("cancelledAppointments", "Cancelaciones", current.cancelledAppointments(), previous.cancelledAppointments()),
                metric("noShowAppointments", "No show", current.noShowAppointments(), previous.noShowAppointments()),
                metric("newPatients", "Pacientes nuevos", current.newPatients(), previous.newPatients()),
                metric("uploadedDocuments", "Documentos cargados", current.uploadedDocuments(), previous.uploadedDocuments()),
                metric("completedAIAnalyses", "Analisis IA completados", current.completedAIAnalyses(), previous.completedAIAnalyses())
            )
        );
    }

    private ComparisonStats loadStats(Instant from, Instant to, BIFilter filter) {
        var sql = """
            select
                (select count(*) from appointments a
                 where a.appointment_type <> 'blocked_slot'
                   and a.status <> 'deleted'
                   and a.start_at >= :from
                   and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as operative_appointments,
                (select count(*) from appointments a
                 where a.appointment_type <> 'blocked_slot'
                   and a.status = 'cancelled'
                   and a.start_at >= :from
                   and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as cancelled_appointments,
                (select count(*) from appointments a
                 where a.appointment_type <> 'blocked_slot'
                   and a.status = 'no_show'
                   and a.start_at >= :from
                   and a.start_at <= :to
            """ + professionalAndSpecialtyFilter("a") + """
                ) as no_show_appointments,
                (select count(*) from patients p
                 where p.status <> 'deleted'
                   and p.created_at >= :from
                   and p.created_at <= :to
                ) as new_patients,
                (select count(*) from clinical_documents d
                 where d.status <> 'deleted'
                   and d.created_at >= :from
                   and d.created_at <= :to
                   and (cast(:professionalId as uuid) is null or d.professional_id = cast(:professionalId as uuid))
                   and (cast(:specialtyId as uuid) is null or exists (
                       select 1 from professional_specialties ps
                       where ps.professional_id = d.professional_id
                         and ps.specialty_id = cast(:specialtyId as uuid)
                         and ps.status = 'active'
                   ))
                ) as uploaded_documents,
                (select count(*) from ai_analyses ai
                 left join clinical_documents d on d.id = ai.clinical_document_id
                 where ai.status = 'completed'
                   and ai.created_at >= :from
                   and ai.created_at <= :to
                   and (cast(:professionalId as uuid) is null or d.professional_id = cast(:professionalId as uuid))
                   and (cast(:specialtyId as uuid) is null or exists (
                       select 1 from professional_specialties ps
                       where ps.professional_id = d.professional_id
                         and ps.specialty_id = cast(:specialtyId as uuid)
                         and ps.status = 'active'
                   ))
                ) as completed_ai_analyses
            """;
        return jdbcTemplate.queryForObject(sql, params(from, to, filter), (rs, rowNum) -> new ComparisonStats(
            longValue(rs, "operative_appointments"),
            longValue(rs, "cancelled_appointments"),
            longValue(rs, "no_show_appointments"),
            longValue(rs, "new_patients"),
            longValue(rs, "uploaded_documents"),
            longValue(rs, "completed_ai_analyses")
        ));
    }

    private MapSqlParameterSource params(Instant from, Instant to, BIFilter filter) {
        return new MapSqlParameterSource()
            .addValue("from", Timestamp.from(from), Types.TIMESTAMP)
            .addValue("to", Timestamp.from(to), Types.TIMESTAMP)
            .addValue("professionalId", filter.professionalId(), Types.OTHER)
            .addValue("specialtyId", filter.specialtyId(), Types.OTHER);
    }

    private BIComparisonMetricResponse metric(String key, String label, long current, long previous) {
        var difference = current - previous;
        if (previous == 0 && current == 0) {
            return new BIComparisonMetricResponse(key, label, current, previous, difference, BigDecimal.ZERO.setScale(2), "0%", "stable");
        }
        if (previous == 0) {
            return new BIComparisonMetricResponse(key, label, current, previous, difference, null, "Nuevo", "new");
        }
        var percentage = BigDecimal.valueOf(difference)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP);
        var type = difference > 0 ? "increase" : difference < 0 ? "decrease" : "stable";
        return new BIComparisonMetricResponse(key, label, current, previous, difference, percentage, percentage + "%", type);
    }

    private record ComparisonStats(
        long operativeAppointments,
        long cancelledAppointments,
        long noShowAppointments,
        long newPatients,
        long uploadedDocuments,
        long completedAIAnalyses
    ) {
    }
}
