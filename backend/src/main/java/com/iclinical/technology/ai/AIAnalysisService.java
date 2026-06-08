package com.iclinical.technology.ai;

import com.iclinical.technology.ai.dto.AIAnalysisResponse;
import com.iclinical.technology.ai.dto.AIAnalysisSummaryResponse;
import com.iclinical.technology.consents.AIConsentService;
import com.iclinical.technology.documents.ClinicalDocument;
import com.iclinical.technology.documents.ClinicalDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AIAnalysisService {

    private static final String ANALYSIS_TYPE = "clinical_document_summary";
    private static final String DISCLAIMER = "Resultado generado por IA. No constituye diagnóstico médico y debe ser revisado por un profesional de salud.";
    private static final Set<String> IN_PROGRESS_STATUSES = Set.of("pending", "processing");
    private static final Set<String> FILTER_STATUSES = Set.of("pending", "processing", "completed", "failed");

    private final AIAnalysisRepository analysisRepository;
    private final ClinicalDocumentRepository documentRepository;
    private final DocumentAIClient documentAIClient;
    private final AIConsentService consentService;

    public AIAnalysisService(
        AIAnalysisRepository analysisRepository,
        ClinicalDocumentRepository documentRepository,
        DocumentAIClient documentAIClient,
        AIConsentService consentService
    ) {
        this.analysisRepository = analysisRepository;
        this.documentRepository = documentRepository;
        this.documentAIClient = documentAIClient;
        this.consentService = consentService;
    }

    @Transactional
    public AIAnalysisResponse requestAnalysis(UUID documentId) {
        var document = findActiveDocument(documentId);
        var consent = requireActiveConsent(document);
        ensureDocumentCanBeAnalyzed(document);

        var analysis = new AIAnalysis();
        analysis.setOrganizationId(document.getOrganizationId());
        analysis.setPatientId(document.getPatientId());
        analysis.setClinicalDocumentId(document.getId());
        analysis.setAiConsentId(consent.getId());
        analysis.setAnalysisType(ANALYSIS_TYPE);
        analysis.setStatus("pending");
        analysis.setSourceSummary(sourceSummary(document));
        var saved = analysisRepository.save(analysis);

        document.setAiAnalysisStatus("pending");
        process(saved, document);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AIAnalysisSummaryResponse> listByDocument(UUID documentId) {
        findActiveDocument(documentId);
        return analysisRepository.findByClinicalDocumentIdAndStatusNotOrderByCreatedAtDesc(documentId, "deleted")
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public AIAnalysisResponse getById(UUID id) {
        return toResponse(findAnalysis(id));
    }

    @Transactional(readOnly = true)
    public List<AIAnalysisSummaryResponse> list(String status, UUID patientId, UUID documentId) {
        var cleanedStatus = clean(status);
        if (StringUtils.hasText(cleanedStatus) && !FILTER_STATUSES.contains(cleanedStatus)) {
            throw new AIAnalysisValidationException("Estado de analisis no permitido");
        }
        if (documentId != null) {
            findActiveDocument(documentId);
        }
        return analysisRepository.findFiltered(cleanedStatus, patientId, documentId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional
    public AIAnalysisResponse retry(UUID id) {
        var failedAnalysis = findAnalysis(id);
        if (!"failed".equals(failedAnalysis.getStatus())) {
            throw new AIAnalysisValidationException("Solo se pueden reintentar analisis fallidos");
        }
        var document = findActiveDocument(failedAnalysis.getClinicalDocumentId());
        var consent = requireActiveConsent(document);
        ensureDocumentCanBeAnalyzed(document);

        var retry = new AIAnalysis();
        retry.setOrganizationId(document.getOrganizationId());
        retry.setPatientId(document.getPatientId());
        retry.setClinicalDocumentId(document.getId());
        retry.setAiConsentId(consent.getId());
        retry.setAnalysisType(ANALYSIS_TYPE);
        retry.setStatus("pending");
        retry.setSourceSummary(sourceSummary(document));
        var saved = analysisRepository.save(retry);

        document.setAiAnalysisStatus("pending");
        process(saved, document);
        return toResponse(saved);
    }

    private void process(AIAnalysis analysis, ClinicalDocument document) {
        analysis.setStatus("processing");
        analysis.setStartedAt(Instant.now());
        document.setAiAnalysisStatus("processing");
        try {
            var result = documentAIClient.analyze(document);
            analysis.setModelName(result.modelName());
            analysis.setClinicalSummary(result.clinicalSummary());
            analysis.setResultSummary(result.clinicalSummary());
            analysis.setRelevantFindings(result.relevantFindings());
            analysis.setMentionedDiagnoses(result.mentionedDiagnoses());
            analysis.setMentionedMedications(result.mentionedMedications());
            analysis.setMentionedAllergies(result.mentionedAllergies());
            analysis.setRecommendations(result.recommendations());
            analysis.setCompletedAt(Instant.now());
            analysis.setStatus("completed");
            document.setAiAnalysisStatus("completed");
        } catch (RuntimeException exception) {
            analysis.setErrorMessage(exception.getMessage());
            analysis.setCompletedAt(Instant.now());
            analysis.setStatus("failed");
            document.setAiAnalysisStatus("failed");
        }
    }

    private com.iclinical.technology.consents.AIConsent requireActiveConsent(ClinicalDocument document) {
        return consentService.findActiveConsent(document.getOrganizationId(), document.getPatientId())
            .orElseThrow(() -> new AIAnalysisValidationException("El paciente no tiene consentimiento IA activo para analisis documental"));
    }

    private void ensureDocumentCanBeAnalyzed(ClinicalDocument document) {
        if (IN_PROGRESS_STATUSES.contains(document.getAiAnalysisStatus())) {
            throw new AIAnalysisValidationException("El documento ya tiene un analisis IA en curso");
        }
        if (analysisRepository.existsActiveForDocument(document.getId(), IN_PROGRESS_STATUSES)) {
            throw new AIAnalysisValidationException("Ya existe un analisis IA pendiente o en procesamiento para el documento");
        }
    }

    private ClinicalDocument findActiveDocument(UUID documentId) {
        if (documentId == null) {
            throw new AIAnalysisValidationException("Debe indicar el documento clinico");
        }
        return documentRepository.findById(documentId)
            .filter(document -> "active".equals(document.getStatus()))
            .orElseThrow(() -> new AIAnalysisValidationException("El documento clinico no existe o no esta activo"));
    }

    private AIAnalysis findAnalysis(UUID id) {
        return analysisRepository.findById(id)
            .filter(analysis -> !"deleted".equals(analysis.getStatus()))
            .orElseThrow(() -> new AIAnalysisNotFoundException("Analisis IA no encontrado"));
    }

    private String sourceSummary(ClinicalDocument document) {
        return "Documento: " + nullSafe(document.getTitle())
            + "; tipo: " + nullSafe(document.getDocumentType())
            + "; archivo: " + nullSafe(document.getOriginalFilename())
            + "; mime: " + nullSafe(document.getMimeType());
    }

    private AIAnalysisResponse toResponse(AIAnalysis analysis) {
        return new AIAnalysisResponse(
            analysis.getId(),
            analysis.getOrganizationId(),
            analysis.getPatientId(),
            analysis.getClinicalDocumentId(),
            analysis.getAiConsentId(),
            analysis.getAnalysisType(),
            analysis.getStatus(),
            analysis.getModelName(),
            analysis.getClinicalSummary(),
            nullToEmpty(analysis.getRelevantFindings()),
            nullToEmpty(analysis.getMentionedDiagnoses()),
            nullToEmpty(analysis.getMentionedMedications()),
            nullToEmpty(analysis.getMentionedAllergies()),
            analysis.getRecommendations(),
            analysis.getErrorMessage(),
            analysis.getStartedAt(),
            analysis.getCompletedAt(),
            analysis.getCreatedAt(),
            analysis.getUpdatedAt(),
            DISCLAIMER
        );
    }

    private AIAnalysisSummaryResponse toSummary(AIAnalysis analysis) {
        return new AIAnalysisSummaryResponse(
            analysis.getId(),
            analysis.getPatientId(),
            analysis.getClinicalDocumentId(),
            analysis.getAiConsentId(),
            analysis.getStatus(),
            analysis.getModelName(),
            analysis.getClinicalSummary(),
            analysis.getErrorMessage(),
            analysis.getCreatedAt(),
            analysis.getCompletedAt()
        );
    }

    private List<String> nullToEmpty(List<String> value) {
        return value == null ? List.of() : value;
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
