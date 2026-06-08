package com.iclinical.technology.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_analyses")
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "clinical_record_id")
    private UUID clinicalRecordId;

    @Column(name = "clinical_document_id")
    private UUID clinicalDocumentId;

    @Column(name = "ai_consent_id")
    private UUID aiConsentId;

    @Column(name = "clinical_image_id")
    private UUID clinicalImageId;

    @Column(name = "requested_by_user_id")
    private UUID requestedByUserId;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "analysis_type", nullable = false, length = 60)
    private String analysisType = "clinical_document_summary";

    @Column(nullable = false, length = 30)
    private String status = "pending";

    @Column(name = "model_name", length = 120)
    private String modelName;

    @Column(name = "source_summary", columnDefinition = "text")
    private String sourceSummary;

    @Column(name = "result_summary", columnDefinition = "text")
    private String resultSummary;

    @Column(name = "clinical_summary", columnDefinition = "text")
    private String clinicalSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "relevant_findings", columnDefinition = "jsonb")
    private List<String> relevantFindings = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mentioned_diagnoses", columnDefinition = "jsonb")
    private List<String> mentionedDiagnoses = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mentioned_medications", columnDefinition = "jsonb")
    private List<String> mentionedMedications = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mentioned_allergies", columnDefinition = "jsonb")
    private List<String> mentionedAllergies = List.of();

    @Column(name = "recommendations", columnDefinition = "text")
    private String recommendations;

    @Column(name = "disclaimer_acknowledged", nullable = false)
    private boolean disclaimerAcknowledged = false;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

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
    public UUID getClinicalRecordId() { return clinicalRecordId; }
    public void setClinicalRecordId(UUID clinicalRecordId) { this.clinicalRecordId = clinicalRecordId; }
    public UUID getClinicalDocumentId() { return clinicalDocumentId; }
    public void setClinicalDocumentId(UUID clinicalDocumentId) { this.clinicalDocumentId = clinicalDocumentId; }
    public UUID getAiConsentId() { return aiConsentId; }
    public void setAiConsentId(UUID aiConsentId) { this.aiConsentId = aiConsentId; }
    public UUID getClinicalImageId() { return clinicalImageId; }
    public void setClinicalImageId(UUID clinicalImageId) { this.clinicalImageId = clinicalImageId; }
    public UUID getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public UUID getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(UUID reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getSourceSummary() { return sourceSummary; }
    public void setSourceSummary(String sourceSummary) { this.sourceSummary = sourceSummary; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public String getClinicalSummary() { return clinicalSummary; }
    public void setClinicalSummary(String clinicalSummary) { this.clinicalSummary = clinicalSummary; }
    public List<String> getRelevantFindings() { return relevantFindings; }
    public void setRelevantFindings(List<String> relevantFindings) { this.relevantFindings = relevantFindings; }
    public List<String> getMentionedDiagnoses() { return mentionedDiagnoses; }
    public void setMentionedDiagnoses(List<String> mentionedDiagnoses) { this.mentionedDiagnoses = mentionedDiagnoses; }
    public List<String> getMentionedMedications() { return mentionedMedications; }
    public void setMentionedMedications(List<String> mentionedMedications) { this.mentionedMedications = mentionedMedications; }
    public List<String> getMentionedAllergies() { return mentionedAllergies; }
    public void setMentionedAllergies(List<String> mentionedAllergies) { this.mentionedAllergies = mentionedAllergies; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public boolean isDisclaimerAcknowledged() { return disclaimerAcknowledged; }
    public void setDisclaimerAcknowledged(boolean disclaimerAcknowledged) { this.disclaimerAcknowledged = disclaimerAcknowledged; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
