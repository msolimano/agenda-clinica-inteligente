package com.iclinical.technology.clinicalrecords;

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
@Table(name = "clinical_records")
public class ClinicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "record_date", nullable = false)
    private Instant recordDate;

    @Column(name = "chief_complaint", columnDefinition = "text")
    private String chiefComplaint;

    @Column(columnDefinition = "text")
    private String anamnesis;

    @Column(name = "physical_exam", columnDefinition = "text")
    private String physicalExam;

    @Column(columnDefinition = "text")
    private String assessment;

    @Column(columnDefinition = "text")
    private String plan;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false, length = 30)
    private String status = "draft";

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (recordDate == null) {
            recordDate = now;
        }
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
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public UUID getAppointmentId() { return appointmentId; }
    public void setAppointmentId(UUID appointmentId) { this.appointmentId = appointmentId; }
    public Instant getRecordDate() { return recordDate; }
    public void setRecordDate(Instant recordDate) { this.recordDate = recordDate; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public String getPhysicalExam() { return physicalExam; }
    public void setPhysicalExam(String physicalExam) { this.physicalExam = physicalExam; }
    public String getAssessment() { return assessment; }
    public void setAssessment(String assessment) { this.assessment = assessment; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(Instant finalizedAt) { this.finalizedAt = finalizedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
