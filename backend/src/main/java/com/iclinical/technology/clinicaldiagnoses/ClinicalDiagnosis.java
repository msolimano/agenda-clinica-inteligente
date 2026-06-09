package com.iclinical.technology.clinicaldiagnoses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_diagnoses")
public class ClinicalDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "clinical_record_id", nullable = false)
    private UUID clinicalRecordId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "diagnosis_catalog_id")
    private UUID diagnosisCatalogId;

    @Column(name = "diagnosis_text", nullable = false, columnDefinition = "text")
    private String diagnosisText;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryDiagnosis;

    @Column(name = "diagnosis_status", nullable = false, length = 40)
    private String diagnosisStatus = "suspected";

    @Column(columnDefinition = "text")
    private String observations;

    @Column(name = "code_system", length = 80)
    private String codeSystem;

    @Column(name = "diagnosis_code", length = 80)
    private String diagnosisCode;

    @Column(name = "diagnosis_code_display", length = 255)
    private String diagnosisCodeDisplay;

    @Column(nullable = false, length = 30)
    private String status = "active";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getClinicalRecordId() { return clinicalRecordId; }
    public void setClinicalRecordId(UUID clinicalRecordId) { this.clinicalRecordId = clinicalRecordId; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public UUID getDiagnosisCatalogId() { return diagnosisCatalogId; }
    public void setDiagnosisCatalogId(UUID diagnosisCatalogId) { this.diagnosisCatalogId = diagnosisCatalogId; }
    public String getDiagnosisText() { return diagnosisText; }
    public void setDiagnosisText(String diagnosisText) { this.diagnosisText = diagnosisText; }
    public boolean isPrimary() { return primaryDiagnosis; }
    public void setPrimary(boolean primary) { this.primaryDiagnosis = primary; }
    public String getDiagnosisStatus() { return diagnosisStatus; }
    public void setDiagnosisStatus(String diagnosisStatus) { this.diagnosisStatus = diagnosisStatus; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getCodeSystem() { return codeSystem; }
    public void setCodeSystem(String codeSystem) { this.codeSystem = codeSystem; }
    public String getDiagnosisCode() { return diagnosisCode; }
    public void setDiagnosisCode(String diagnosisCode) { this.diagnosisCode = diagnosisCode; }
    public String getDiagnosisCodeDisplay() { return diagnosisCodeDisplay; }
    public void setDiagnosisCodeDisplay(String diagnosisCodeDisplay) { this.diagnosisCodeDisplay = diagnosisCodeDisplay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
