package com.iclinical.technology.documents;

import com.iclinical.technology.documents.dto.ClinicalDocumentCreateRequest;
import com.iclinical.technology.documents.dto.ClinicalDocumentResponse;
import com.iclinical.technology.documents.dto.ClinicalDocumentSummaryResponse;
import com.iclinical.technology.patients.Patient;
import com.iclinical.technology.patients.PatientRepository;
import com.iclinical.technology.professionals.Professional;
import com.iclinical.technology.professionals.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;

@Service
public class ClinicalDocumentService {

    private static final Set<String> DOCUMENT_TYPES = Set.of(
        "medical_report",
        "laboratory_exam",
        "imaging_exam",
        "prescription",
        "medical_order",
        "certificate",
        "other"
    );
    private static final Map<String, Set<String>> MIME_EXTENSIONS = Map.of(
        "application/pdf", Set.of(".pdf"),
        "image/jpeg", Set.of(".jpg", ".jpeg"),
        "image/png", Set.of(".png"),
        "application/msword", Set.of(".doc"),
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of(".docx")
    );

    private final ClinicalDocumentRepository documentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final LocalClinicalDocumentStorageService storageService;
    private final ClinicalDocumentStorageProperties storageProperties;

    public ClinicalDocumentService(
        ClinicalDocumentRepository documentRepository,
        PatientRepository patientRepository,
        ProfessionalRepository professionalRepository,
        LocalClinicalDocumentStorageService storageService,
        ClinicalDocumentStorageProperties storageProperties
    ) {
        this.documentRepository = documentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    @Transactional(readOnly = true)
    public List<ClinicalDocumentSummaryResponse> list(UUID patientId, String documentType) {
        var type = clean(documentType);
        if (StringUtils.hasText(type) && !DOCUMENT_TYPES.contains(type)) {
            throw new ClinicalDocumentValidationException("Tipo de documento no permitido");
        }
        if (patientId != null) {
            findPatient(patientId, false);
        }
        return documentRepository.findActiveFiltered(patientId, type)
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalDocumentSummaryResponse> listByPatient(UUID patientId) {
        findPatient(patientId, false);
        return documentRepository.findByPatientIdAndStatusNotOrderByCreatedAtDesc(patientId, "deleted")
            .stream()
            .map(this::toSummary)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicalDocumentResponse getById(UUID id) {
        return toResponse(findDocument(id));
    }

    @Transactional
    public ClinicalDocumentResponse create(ClinicalDocumentCreateRequest request) {
        var patient = findPatient(request.patientId(), true);
        var professional = resolveProfessional(request.professionalId(), patient);
        var documentType = resolveDocumentType(request.documentType());
        var title = requireTitle(request.title());
        var description = clean(request.description());
        validateFile(request.file());

        var stored = storageService.store(patient.getOrganizationId(), patient.getId(), request.file());

        var document = new ClinicalDocument();
        document.setOrganizationId(patient.getOrganizationId());
        document.setPatientId(patient.getId());
        document.setProfessionalId(professional == null ? null : professional.getId());
        document.setDocumentType(documentType);
        document.setTitle(title);
        document.setDescription(description);
        document.setOriginalFilename(stored.fileName());
        document.setMimeType(stored.mimeType());
        document.setFileSizeBytes(stored.fileSize());
        document.setStorageProvider("local");
        document.setObjectKey(stored.objectKey());
        document.setChecksumSha256(stored.checksumSha256());
        document.setAiAnalysisStatus("not_requested");
        document.setStatus("active");

        return toResponse(documentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public ClinicalDocumentDownload download(UUID id) {
        var document = findDocument(id);
        var resource = storageService.load(document.getObjectKey());
        var fileSize = document.getFileSizeBytes() == null ? 0L : document.getFileSizeBytes();
        return new ClinicalDocumentDownload(document.getOriginalFilename(), document.getMimeType(), fileSize, resource);
    }

    @Transactional
    public ClinicalDocumentResponse delete(UUID id) {
        var document = findDocument(id);
        document.setStatus("deleted");
        return toResponse(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClinicalDocumentValidationException("Debe adjuntar un archivo clinico");
        }
        if (file.getSize() <= 0) {
            throw new ClinicalDocumentValidationException("El archivo debe tener contenido");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new ClinicalDocumentValidationException("El archivo supera el tamano maximo permitido");
        }
        var mimeType = clean(file.getContentType());
        if (!StringUtils.hasText(mimeType) || !storageProperties.getAllowedMimeTypes().contains(mimeType)) {
            throw new ClinicalDocumentValidationException("Formato de archivo no permitido");
        }
        var filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        var allowedExtensions = MIME_EXTENSIONS.getOrDefault(mimeType, Set.of());
        if (allowedExtensions.stream().noneMatch(filename::endsWith)) {
            throw new ClinicalDocumentValidationException("La extension del archivo no coincide con el tipo MIME permitido");
        }
    }

    private Patient findPatient(UUID patientId, boolean requireActive) {
        if (patientId == null) {
            throw new ClinicalDocumentValidationException("Debe indicar el paciente asociado al documento");
        }
        return patientRepository.findById(patientId)
            .filter(patient -> !"deleted".equals(patient.getStatus()))
            .filter(patient -> !requireActive || "active".equals(patient.getStatus()))
            .orElseThrow(() -> new ClinicalDocumentValidationException("El paciente indicado no existe o no esta activo"));
    }

    private Professional resolveProfessional(UUID professionalId, Patient patient) {
        if (professionalId == null) {
            return null;
        }
        var professional = professionalRepository.findById(professionalId)
            .filter(item -> "active".equals(item.getStatus()))
            .orElseThrow(() -> new ClinicalDocumentValidationException("El profesional indicado no existe o no esta activo"));
        if (!professional.getOrganizationId().equals(patient.getOrganizationId())) {
            throw new ClinicalDocumentValidationException("Profesional y paciente deben pertenecer a la misma organizacion");
        }
        return professional;
    }

    private ClinicalDocument findDocument(UUID id) {
        return documentRepository.findById(id)
            .filter(document -> !"deleted".equals(document.getStatus()))
            .orElseThrow(() -> new ClinicalDocumentNotFoundException("Documento clinico no encontrado"));
    }

    private String resolveDocumentType(String value) {
        var type = clean(value);
        if (!StringUtils.hasText(type) || !DOCUMENT_TYPES.contains(type)) {
            throw new ClinicalDocumentValidationException("Tipo de documento no permitido");
        }
        return type;
    }

    private String requireTitle(String value) {
        var title = clean(value);
        if (!StringUtils.hasText(title)) {
            throw new ClinicalDocumentValidationException("Debe indicar el titulo del documento");
        }
        if (title.length() > 180) {
            throw new ClinicalDocumentValidationException("El titulo no puede superar 180 caracteres");
        }
        return title;
    }

    private ClinicalDocumentResponse toResponse(ClinicalDocument document) {
        return new ClinicalDocumentResponse(
            document.getId(),
            document.getOrganizationId(),
            document.getPatientId(),
            patientName(document.getPatientId()),
            document.getProfessionalId(),
            professionalName(document.getProfessionalId()),
            document.getDocumentType(),
            document.getTitle(),
            document.getDescription(),
            document.getOriginalFilename(),
            document.getMimeType(),
            document.getFileSizeBytes() == null ? 0L : document.getFileSizeBytes(),
            document.getObjectKey(),
            document.getChecksumSha256(),
            document.getAiAnalysisStatus(),
            document.getStatus(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }

    private ClinicalDocumentSummaryResponse toSummary(ClinicalDocument document) {
        return new ClinicalDocumentSummaryResponse(
            document.getId(),
            document.getPatientId(),
            patientName(document.getPatientId()),
            document.getProfessionalId(),
            professionalName(document.getProfessionalId()),
            document.getDocumentType(),
            document.getTitle(),
            document.getOriginalFilename(),
            document.getMimeType(),
            document.getFileSizeBytes() == null ? 0L : document.getFileSizeBytes(),
            document.getStatus(),
            document.getAiAnalysisStatus(),
            document.getCreatedAt()
        );
    }

    private String patientName(UUID patientId) {
        return patientRepository.findById(patientId)
            .map(patient -> patient.getFirstName() + " " + patient.getLastName())
            .orElse("Paciente no disponible");
    }

    private String professionalName(UUID professionalId) {
        if (professionalId == null) {
            return null;
        }
        return professionalRepository.findById(professionalId)
            .map(professional -> professional.getFirstName() + " " + professional.getLastName())
            .orElse("Profesional no disponible");
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
