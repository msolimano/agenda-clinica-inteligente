package com.iclinical.technology.patientportal;

import com.iclinical.technology.patientportal.dto.PatientPortalAIConsentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalAppointmentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalClinicalRecordResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDiagnosisResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalDocumentResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalEvolutionResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalPatientResponse;
import com.iclinical.technology.patientportal.dto.PatientPortalPrescriptionResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PatientPortalRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PatientPortalRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<PatientPortalPatientResponse> findActivePatient(UUID patientId) {
        var sql = """
            select id, organization_id, document_type, document_number, first_name, last_name,
                   birth_date, sex, email, phone, address, emergency_contact_name,
                   emergency_contact_phone, emergency_contact_relationship, status, created_at, updated_at
            from patients
            where id = :patientId
              and status <> 'deleted'
            """;
        var result = jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> patient(rs));
        return result.stream().findFirst();
    }

    public long countUpcomingAppointments(UUID patientId) {
        return count("""
            select count(*) from appointments
            where patient_id = :patientId
              and status <> 'deleted'
              and appointment_type <> 'blocked_slot'
              and start_at >= now()
            """, patientId);
    }

    public long countHistoricalAppointments(UUID patientId) {
        return count("""
            select count(*) from appointments
            where patient_id = :patientId
              and status <> 'deleted'
              and appointment_type <> 'blocked_slot'
              and start_at < now()
            """, patientId);
    }

    public long countActiveDocuments(UUID patientId) {
        return count("""
            select count(*) from clinical_documents
            where patient_id = :patientId
              and status = 'active'
            """, patientId);
    }

    public long countActivePrescriptions(UUID patientId) {
        return count("""
            select count(*) from clinical_prescriptions
            where patient_id = :patientId
              and status = 'active'
              and prescription_status in ('draft', 'active')
            """, patientId);
    }

    public long countActiveDiagnoses(UUID patientId) {
        return count("""
            select count(*) from clinical_diagnoses
            where patient_id = :patientId
              and status = 'active'
              and diagnosis_status <> 'ruled_out'
            """, patientId);
    }

    public long countClinicalRecords(UUID patientId) {
        return count("""
            select count(*) from clinical_records
            where patient_id = :patientId
              and status <> 'deleted'
            """, patientId);
    }

    public List<PatientPortalAppointmentResponse> appointments(UUID patientId) {
        var sql = """
            select a.id, a.professional_id, concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   a.start_at, a.end_at, a.appointment_type, a.status, a.reason,
                   case when a.start_at >= now() then true else false end as upcoming
            from appointments a
            join professionals p on p.id = a.professional_id
            where a.patient_id = :patientId
              and a.status <> 'deleted'
              and a.appointment_type <> 'blocked_slot'
            order by a.start_at desc
            limit 80
            """;
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> new PatientPortalAppointmentResponse(
            uuid(rs, "id"),
            uuid(rs, "professional_id"),
            rs.getString("professional_name"),
            instant(rs, "start_at"),
            instant(rs, "end_at"),
            rs.getString("appointment_type"),
            rs.getString("status"),
            rs.getString("reason"),
            rs.getBoolean("upcoming")
        ));
    }

    public List<PatientPortalDocumentResponse> documents(UUID patientId) {
        var sql = """
            select id, clinical_record_id, document_type, title, description, original_filename,
                   mime_type, file_size_bytes, ai_analysis_status, status, created_at
            from clinical_documents
            where patient_id = :patientId
              and status = 'active'
            order by created_at desc
            """;
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> {
            var id = uuid(rs, "id");
            return new PatientPortalDocumentResponse(
                id,
                nullableUuid(rs, "clinical_record_id"),
                rs.getString("document_type"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("original_filename"),
                rs.getString("mime_type"),
                rs.getLong("file_size_bytes"),
                rs.getString("ai_analysis_status"),
                rs.getString("status"),
                "/api/clinical-documents/" + id + "/download",
                instant(rs, "created_at")
            );
        });
    }

    public List<PatientPortalPrescriptionResponse> prescriptions(UUID patientId) {
        var sql = """
            select cp.id, cp.clinical_record_id, cp.diagnosis_id, cd.diagnosis_text,
                   concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   cp.medication_name, cp.dosage, cp.frequency, cp.duration, cp.route,
                   cp.patient_instructions, cp.clinical_notes, cp.prescription_status,
                   cp.created_at, cp.updated_at
            from clinical_prescriptions cp
            join professionals p on p.id = cp.professional_id
            left join clinical_diagnoses cd on cd.id = cp.diagnosis_id
            where cp.patient_id = :patientId
              and cp.status = 'active'
            order by case when cp.prescription_status in ('draft', 'active') then 0 else 1 end,
                     cp.created_at desc
            """;
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> {
            var prescriptionStatus = rs.getString("prescription_status");
            return new PatientPortalPrescriptionResponse(
                uuid(rs, "id"),
                uuid(rs, "clinical_record_id"),
                nullableUuid(rs, "diagnosis_id"),
                rs.getString("diagnosis_text"),
                rs.getString("professional_name"),
                rs.getString("medication_name"),
                rs.getString("dosage"),
                rs.getString("frequency"),
                rs.getString("duration"),
                rs.getString("route"),
                rs.getString("patient_instructions"),
                rs.getString("clinical_notes"),
                prescriptionStatus,
                "suspended".equals(prescriptionStatus) || "completed".equals(prescriptionStatus) || "cancelled".equals(prescriptionStatus),
                instant(rs, "created_at"),
                instant(rs, "updated_at")
            );
        });
    }

    public List<PatientPortalDiagnosisResponse> diagnoses(UUID patientId) {
        var sql = """
            select cd.id, cd.clinical_record_id, concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   cd.diagnosis_text, cd.is_primary, cd.diagnosis_status, cd.observations,
                   cd.code_system, cd.diagnosis_code, cd.diagnosis_code_display, cd.created_at, cd.updated_at
            from clinical_diagnoses cd
            join professionals p on p.id = cd.professional_id
            where cd.patient_id = :patientId
              and cd.status = 'active'
            order by cd.is_primary desc, cd.created_at desc
            """;
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> {
            var status = rs.getString("diagnosis_status");
            return new PatientPortalDiagnosisResponse(
                uuid(rs, "id"),
                uuid(rs, "clinical_record_id"),
                rs.getString("professional_name"),
                rs.getString("diagnosis_text"),
                rs.getBoolean("is_primary"),
                status,
                "ruled_out".equals(status) ? "Descartado" : displayDiagnosisStatus(status),
                rs.getString("observations"),
                rs.getString("code_system"),
                rs.getString("diagnosis_code"),
                rs.getString("diagnosis_code_display"),
                instant(rs, "created_at"),
                instant(rs, "updated_at")
            );
        });
    }

    public List<PatientPortalClinicalRecordResponse> clinicalRecords(UUID patientId) {
        var sql = """
            select cr.id, cr.professional_id, concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   cr.appointment_id, cr.record_date, cr.chief_complaint, cr.assessment, cr.plan, cr.status
            from clinical_records cr
            join professionals p on p.id = cr.professional_id
            where cr.patient_id = :patientId
              and cr.status <> 'deleted'
            order by cr.record_date desc
            """;
        var evolutions = evolutions(patientId).stream().collect(Collectors.groupingBy(PatientPortalEvolutionResponse::clinicalRecordId));
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> {
            var id = uuid(rs, "id");
            return new PatientPortalClinicalRecordResponse(
                id,
                uuid(rs, "professional_id"),
                rs.getString("professional_name"),
                nullableUuid(rs, "appointment_id"),
                instant(rs, "record_date"),
                rs.getString("chief_complaint"),
                rs.getString("assessment"),
                rs.getString("plan"),
                rs.getString("status"),
                evolutions.getOrDefault(id, List.of())
            );
        });
    }

    public List<PatientPortalEvolutionResponse> evolutions(UUID patientId) {
        var sql = """
            select ce.id, ce.clinical_record_id, concat_ws(' ', p.first_name, p.last_name) as professional_name,
                   ce.evolution_date, ce.subjective, ce.objective, ce.assessment, ce.plan, ce.notes,
                   ce.evolution_status, ce.created_at, ce.updated_at
            from clinical_evolutions ce
            join professionals p on p.id = ce.professional_id
            where ce.patient_id = :patientId
              and ce.status <> 'deleted'
            order by ce.evolution_date desc
            """;
        return jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> {
            var status = rs.getString("evolution_status");
            return new PatientPortalEvolutionResponse(
                uuid(rs, "id"),
                uuid(rs, "clinical_record_id"),
                rs.getString("professional_name"),
                instant(rs, "evolution_date"),
                rs.getString("subjective"),
                rs.getString("objective"),
                rs.getString("assessment"),
                rs.getString("plan"),
                rs.getString("notes"),
                status,
                "cancelled".equals(status),
                instant(rs, "created_at"),
                instant(rs, "updated_at")
            );
        });
    }

    public PatientPortalAIConsentResponse aiConsent(UUID patientId) {
        var sql = """
            select id, consent_type, consent_version, granted, granted_at, revoked_at, status
            from consents
            where patient_id = :patientId
              and consent_type = 'ai_analysis'
              and status <> 'deleted'
            order by case when status = 'active' and granted = true then 0 else 1 end, created_at desc
            limit 1
            """;
        var result = jdbcTemplate.query(sql, params(patientId), (rs, rowNum) -> new PatientPortalAIConsentResponse(
            uuid(rs, "id"),
            rs.getBoolean("granted") && "active".equals(rs.getString("status")),
            rs.getString("consent_type"),
            rs.getString("consent_version"),
            instant(rs, "granted_at"),
            instant(rs, "revoked_at"),
            rs.getString("status")
        ));
        return result.stream().findFirst().orElse(new PatientPortalAIConsentResponse(null, false, "ai_analysis", null, null, null, "not_registered"));
    }

    private long count(String sql, UUID patientId) {
        var value = jdbcTemplate.queryForObject(sql, params(patientId), Long.class);
        return value == null ? 0L : value;
    }

    private PatientPortalPatientResponse patient(ResultSet rs) throws SQLException {
        return new PatientPortalPatientResponse(
            uuid(rs, "id"),
            uuid(rs, "organization_id"),
            rs.getString("document_type"),
            rs.getString("document_number"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            localDate(rs, "birth_date"),
            rs.getString("sex"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("emergency_contact_name"),
            rs.getString("emergency_contact_phone"),
            rs.getString("emergency_contact_relationship"),
            rs.getString("status"),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
        );
    }

    private MapSqlParameterSource params(UUID patientId) {
        return new MapSqlParameterSource().addValue("patientId", patientId, Types.OTHER);
    }

    private UUID uuid(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, UUID.class);
    }

    private UUID nullableUuid(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, UUID.class);
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private LocalDate localDate(ResultSet rs, String column) throws SQLException {
        var date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    private String displayDiagnosisStatus(String status) {
        return switch (status) {
            case "confirmed" -> "Confirmado";
            case "resolved" -> "Resuelto";
            case "suspected" -> "Sospechado";
            default -> status;
        };
    }
}
