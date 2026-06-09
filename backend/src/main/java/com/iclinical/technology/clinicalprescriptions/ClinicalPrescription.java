package com.iclinical.technology.clinicalprescriptions;

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
@Table(name = "clinical_prescriptions")
public class ClinicalPrescription {

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

    @Column(name = "diagnosis_id")
    private UUID diagnosisId;

    @Column(name = "medication_name", nullable = false, columnDefinition = "text")
    private String medicationName;

    @Column(nullable = false, columnDefinition = "text")
    private String dosage;

    @Column(nullable = false, columnDefinition = "text")
    private String frequency;

    @Column(columnDefinition = "text")
    private String duration;

    @Column(columnDefinition = "text")
    private String route;

    @Column(name = "patient_instructions", columnDefinition = "text")
    private String patientInstructions;

    @Column(name = "clinical_notes", columnDefinition = "text")
    private String clinicalNotes;

    @Column(name = "prescription_status", nullable = false, length = 40)
    private String prescriptionStatus = "draft";

    @Column(nullable = false, length = 30)
    private String status = "active";

    @Column(name = "medication_catalog_id")
    private UUID medicationCatalogId;

    @Column(name = "medication_code", length = 80)
    private String medicationCode;

    @Column(name = "medication_code_system", length = 80)
    private String medicationCodeSystem;

    @Column(name = "medication_code_display", length = 255)
    private String medicationCodeDisplay;

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
    public UUID getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(UUID diagnosisId) { this.diagnosisId = diagnosisId; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getPatientInstructions() { return patientInstructions; }
    public void setPatientInstructions(String patientInstructions) { this.patientInstructions = patientInstructions; }
    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }
    public String getPrescriptionStatus() { return prescriptionStatus; }
    public void setPrescriptionStatus(String prescriptionStatus) { this.prescriptionStatus = prescriptionStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getMedicationCatalogId() { return medicationCatalogId; }
    public void setMedicationCatalogId(UUID medicationCatalogId) { this.medicationCatalogId = medicationCatalogId; }
    public String getMedicationCode() { return medicationCode; }
    public void setMedicationCode(String medicationCode) { this.medicationCode = medicationCode; }
    public String getMedicationCodeSystem() { return medicationCodeSystem; }
    public void setMedicationCodeSystem(String medicationCodeSystem) { this.medicationCodeSystem = medicationCodeSystem; }
    public String getMedicationCodeDisplay() { return medicationCodeDisplay; }
    public void setMedicationCodeDisplay(String medicationCodeDisplay) { this.medicationCodeDisplay = medicationCodeDisplay; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
