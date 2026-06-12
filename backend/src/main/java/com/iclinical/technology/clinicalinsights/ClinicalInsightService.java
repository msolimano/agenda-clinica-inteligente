package com.iclinical.technology.clinicalinsights;

import com.iclinical.technology.ai.AIAnalysis;
import com.iclinical.technology.ai.DocumentAIInsight;
import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightResponse;
import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightReviewRequest;
import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightSummaryResponse;
import com.iclinical.technology.documents.ClinicalDocument;
import com.iclinical.technology.documents.ClinicalDocumentRepository;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ClinicalInsightService {

    private static final String DISCLAIMER = "Sugerencia generada por IA. Debe ser revisada por un profesional de salud. No constituye diagnostico medico.";
    private static final Set<String> INSIGHT_TYPES = Set.of(
        "clinical_summary",
        "clinical_alert",
        "diagnosis_candidate",
        "medication_candidate",
        "allergy_candidate",
        "risk_factor_candidate",
        "lab_result_candidate",
        "observation_candidate"
    );
    private static final Set<String> REVIEW_STATUSES = Set.of("accepted", "rejected", "dismissed");
    private static final Set<String> FILTER_STATUSES = Set.of("pending", "accepted", "rejected", "dismissed");

    private final ClinicalInsightRepository insightRepository;
    private final ClinicalDocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;

    public ClinicalInsightService(
        ClinicalInsightRepository insightRepository,
        ClinicalDocumentRepository documentRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository
    ) {
        this.insightRepository = insightRepository;
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ClinicalInsightSummaryResponse> list(UUID patientId, UUID documentId, String status, String type) {
        var cleanedStatus = resolveOptionalStatus(status);
        var cleanedType = resolveOptionalType(type);
        if (patientId != null) {
            findPatient(patientId);
        }
        if (documentId != null) {
            findDocument(documentId);
        }
        return insightRepository.findAll(filterSpecification(patientId, documentId, cleanedStatus, cleanedType), Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalInsightSummaryResponse> listByPatient(UUID patientId) {
        findPatient(patientId);
        return insightRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalInsightSummaryResponse> listByDocument(UUID documentId) {
        findDocument(documentId);
        return insightRepository.findByClinicalDocumentIdOrderByCreatedAtDesc(documentId)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalInsightResponse getById(UUID id) {
        return toResponse(findInsight(id));
    }

    @Transactional
    public ClinicalInsightResponse accept(UUID id, ClinicalInsightReviewRequest request) {
        return review(id, "accepted", request);
    }

    @Transactional
    public ClinicalInsightResponse reject(UUID id, ClinicalInsightReviewRequest request) {
        return review(id, "rejected", request);
    }

    @Transactional
    public ClinicalInsightResponse dismiss(UUID id, ClinicalInsightReviewRequest request) {
        return review(id, "dismissed", request);
    }

    @Transactional
    public List<ClinicalInsightSummaryResponse> createFromAnalysis(AIAnalysis analysis, ClinicalDocument document, List<DocumentAIInsight> suggestions) {
        if (analysis == null || document == null) {
            return List.of();
        }

        var normalized = normalizeSuggestions(document, suggestions);
        var seen = new LinkedHashSet<String>();
        var insights = normalized.stream()
            .filter(suggestion -> seen.add(deduplicationKey(suggestion)))
            .map(suggestion -> buildInsight(analysis, document, suggestion))
            .toList();

        return insightRepository.saveAll(insights)
            .stream()
            .map(this::toSummary)
            .toList();
    }


    private Specification<ClinicalInsight> filterSpecification(UUID patientId, UUID documentId, String status, String type) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (patientId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("patientId"), patientId));
            }
            if (documentId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("clinicalDocumentId"), documentId));
            }
            if (StringUtils.hasText(status)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(type)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("insightType"), type));
            }
            return predicate;
        };
    }

    private ClinicalInsightResponse review(UUID id, String status, ClinicalInsightReviewRequest request) {
        if (!REVIEW_STATUSES.contains(status)) {
            throw new ClinicalInsightValidationException("Estado de revision no permitido");
        }
        var insight = findInsight(id);
        var reviewerId = request == null ? null : request.reviewedByProfessionalId();
        if (reviewerId != null) {
            var reviewer = professionalRepository.findById(reviewerId)
                .filter(professional -> "active".equals(professional.getStatus()))
                .orElseThrow(() -> new ClinicalInsightValidationException("El profesional revisor no existe o no esta activo"));
            if (!reviewer.getOrganizationId().equals(insight.getOrganizationId())) {
                throw new ClinicalInsightValidationException("El profesional revisor debe pertenecer a la misma organizacion");
            }
        }
        insight.setStatus(status);
        insight.setReviewedAt(Instant.now());
        insight.setReviewedByProfessionalId(reviewerId);
        insight.setReviewNotes(clean(request == null ? null : request.reviewNotes()));
        return toResponse(insight);
    }

    private ClinicalInsight buildInsight(AIAnalysis analysis, ClinicalDocument document, DocumentAIInsight suggestion) {
        var insight = new ClinicalInsight();
        insight.setOrganizationId(document.getOrganizationId());
        insight.setPatientId(document.getPatientId());
        insight.setClinicalDocumentId(document.getId());
        insight.setClinicalRecordId(document.getClinicalRecordId());
        insight.setProfessionalId(document.getProfessionalId());
        insight.setAiAnalysisId(analysis.getId());
        insight.setSourceDocumentName(sourceDocumentName(document));
        insight.setInsightType(resolveInsightType(suggestion.type(), "observation_candidate"));
        insight.setTitle(requireTitle(suggestion.title()));
        insight.setDescription(requireDescription(suggestion.description()));
        insight.setSourceText(clean(suggestion.sourceText()));
        insight.setConfidence(resolveConfidence(suggestion.confidence()));
        insight.setStatus("pending");
        return insight;
    }

    private List<DocumentAIInsight> normalizeSuggestions(ClinicalDocument document, List<DocumentAIInsight> suggestions) {
        if (suggestions != null && !suggestions.isEmpty()) {
            return suggestions.stream()
                .filter(suggestion -> suggestion != null)
                .filter(suggestion -> StringUtils.hasText(suggestion.title()) || StringUtils.hasText(suggestion.description()))
                .toList();
        }
        return List.of(new DocumentAIInsight(
            "clinical_summary",
            "Documento para revision clinica",
            "Documento cargado para revision clinica. No se pudo extraer contenido estructurado en esta version.",
            sourceDocumentName(document),
            null
        ));
    }

    private String deduplicationKey(DocumentAIInsight suggestion) {
        return (clean(suggestion.type()) + "|" + clean(suggestion.title()) + "|" + clean(suggestion.description())).toLowerCase();
    }

    private ClinicalInsight findInsight(UUID id) {
        if (id == null) {
            throw new ClinicalInsightValidationException("Debe indicar el insight clinico");
        }
        return insightRepository.findById(id)
            .orElseThrow(() -> new ClinicalInsightNotFoundException("Insight clinico no encontrado"));
    }

    private void findPatient(UUID patientId) {
        if (patientId == null) {
            throw new ClinicalInsightValidationException("Debe indicar el paciente");
        }
        patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalInsightValidationException("Paciente no encontrado o eliminado"));
    }

    private ClinicalDocument findDocument(UUID documentId) {
        if (documentId == null) {
            throw new ClinicalInsightValidationException("Debe indicar el documento clinico");
        }
        return documentRepository.findById(documentId)
            .filter(document -> !"deleted".equals(document.getStatus()))
            .orElseThrow(() -> new ClinicalInsightValidationException("Documento clinico no encontrado o eliminado"));
    }

    private String resolveOptionalStatus(String value) {
        var status = clean(value);
        if (!StringUtils.hasText(status)) {
            return null;
        }
        if (!FILTER_STATUSES.contains(status)) {
            throw new ClinicalInsightValidationException("Estado de insight no permitido");
        }
        return status;
    }

    private String resolveOptionalType(String value) {
        var type = clean(value);
        if (!StringUtils.hasText(type)) {
            return null;
        }
        if (!INSIGHT_TYPES.contains(type)) {
            throw new ClinicalInsightValidationException("Tipo de insight no permitido");
        }
        return type;
    }

    private String resolveInsightType(String value, String fallback) {
        var type = clean(value);
        if (!StringUtils.hasText(type)) {
            return fallback;
        }
        return INSIGHT_TYPES.contains(type) ? type : fallback;
    }

    private String requireTitle(String value) {
        var title = clean(value);
        if (!StringUtils.hasText(title)) {
            throw new ClinicalInsightValidationException("Debe indicar el titulo del insight");
        }
        return title.length() > 180 ? title.substring(0, 180) : title;
    }

    private String requireDescription(String value) {
        var description = clean(value);
        if (!StringUtils.hasText(description)) {
            throw new ClinicalInsightValidationException("Debe indicar la descripcion del insight");
        }
        return description;
    }

    private BigDecimal resolveConfidence(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return null;
        }
        var clamped = Math.max(0.0, Math.min(1.0, value));
        return BigDecimal.valueOf(clamped).setScale(4, RoundingMode.HALF_UP);
    }

    private String sourceDocumentName(ClinicalDocument document) {
        var fileName = clean(document.getOriginalFilename());
        if (StringUtils.hasText(fileName)) {
            return fileName;
        }
        var title = clean(document.getTitle());
        return StringUtils.hasText(title) ? title : "Documento clinico";
    }

    private ClinicalInsightResponse toResponse(ClinicalInsight insight) {
        return new ClinicalInsightResponse(
            insight.getId(),
            insight.getOrganizationId(),
            insight.getPatientId(),
            insight.getClinicalDocumentId(),
            insight.getClinicalRecordId(),
            insight.getProfessionalId(),
            insight.getAiAnalysisId(),
            insight.getSourceDocumentName(),
            insight.getInsightType(),
            insight.getTitle(),
            insight.getDescription(),
            insight.getSourceText(),
            insight.getConfidence(),
            insight.getStatus(),
            insight.getCreatedAt(),
            insight.getUpdatedAt(),
            insight.getReviewedAt(),
            insight.getReviewedByProfessionalId(),
            insight.getReviewNotes(),
            DISCLAIMER
        );
    }

    private ClinicalInsightSummaryResponse toSummary(ClinicalInsight insight) {
        return new ClinicalInsightSummaryResponse(
            insight.getId(),
            insight.getOrganizationId(),
            insight.getPatientId(),
            insight.getClinicalDocumentId(),
            insight.getClinicalRecordId(),
            insight.getProfessionalId(),
            insight.getAiAnalysisId(),
            insight.getSourceDocumentName(),
            insight.getInsightType(),
            insight.getTitle(),
            insight.getDescription(),
            insight.getSourceText(),
            insight.getConfidence(),
            insight.getStatus(),
            insight.getCreatedAt(),
            insight.getUpdatedAt(),
            insight.getReviewedAt(),
            insight.getReviewedByProfessionalId(),
            insight.getReviewNotes(),
            DISCLAIMER
        );
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
