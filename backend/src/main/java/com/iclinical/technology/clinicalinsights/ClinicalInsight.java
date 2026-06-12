package com.iclinical.technology.clinicalinsights;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_insights")
public class ClinicalInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "clinical_document_id", nullable = false)
    private UUID clinicalDocumentId;

    @Column(name = "clinical_record_id")
    private UUID clinicalRecordId;

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "ai_analysis_id")
    private UUID aiAnalysisId;

    @Column(name = "source_document_name", nullable = false, length = 255)
    private String sourceDocumentName;

    @Column(name = "insight_type", nullable = false, length = 60)
    private String insightType;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "source_text", columnDefinition = "text")
    private String sourceText;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(nullable = false, length = 30)
    private String status = "pending";

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by_professional_id")
    private UUID reviewedByProfessionalId;

    @Column(name = "review_notes", columnDefinition = "text")
    private String reviewNotes;

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
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getClinicalDocumentId() { return clinicalDocumentId; }
    public void setClinicalDocumentId(UUID clinicalDocumentId) { this.clinicalDocumentId = clinicalDocumentId; }
    public UUID getClinicalRecordId() { return clinicalRecordId; }
    public void setClinicalRecordId(UUID clinicalRecordId) { this.clinicalRecordId = clinicalRecordId; }
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public UUID getAiAnalysisId() { return aiAnalysisId; }
    public void setAiAnalysisId(UUID aiAnalysisId) { this.aiAnalysisId = aiAnalysisId; }
    public String getSourceDocumentName() { return sourceDocumentName; }
    public void setSourceDocumentName(String sourceDocumentName) { this.sourceDocumentName = sourceDocumentName; }
    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSourceText() { return sourceText; }
    public void setSourceText(String sourceText) { this.sourceText = sourceText; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public UUID getReviewedByProfessionalId() { return reviewedByProfessionalId; }
    public void setReviewedByProfessionalId(UUID reviewedByProfessionalId) { this.reviewedByProfessionalId = reviewedByProfessionalId; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
