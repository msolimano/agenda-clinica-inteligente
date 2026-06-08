package com.iclinical.technology.documents;

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
@Table(name = "clinical_documents")
public class ClinicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "clinical_record_id")
    private UUID clinicalRecordId;

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "document_type", nullable = false, length = 60)
    private String documentType;

    @Column(length = 180)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_provider", nullable = false, length = 30)
    private String storageProvider = "local";

    @Column(name = "bucket_name", length = 160)
    private String bucketName;

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "storage_url", columnDefinition = "text")
    private String storageUrl;

    @Column(name = "checksum_sha256", length = 128)
    private String checksumSha256;

    @Column(name = "ai_analysis_status", nullable = false, length = 40)
    private String aiAnalysisStatus = "not_requested";

    @Column(nullable = false, length = 30)
    private String status = "active";

    @Column(name = "uploaded_by_user_id")
    private UUID uploadedByUserId;

    @Column(name = "related_clinical_image_id")
    private UUID relatedClinicalImageId;

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
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getStorageUrl() { return storageUrl; }
    public void setStorageUrl(String storageUrl) { this.storageUrl = storageUrl; }
    public String getChecksumSha256() { return checksumSha256; }
    public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }
    public String getAiAnalysisStatus() { return aiAnalysisStatus; }
    public void setAiAnalysisStatus(String aiAnalysisStatus) { this.aiAnalysisStatus = aiAnalysisStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(UUID uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
    public UUID getRelatedClinicalImageId() { return relatedClinicalImageId; }
    public void setRelatedClinicalImageId(UUID relatedClinicalImageId) { this.relatedClinicalImageId = relatedClinicalImageId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
