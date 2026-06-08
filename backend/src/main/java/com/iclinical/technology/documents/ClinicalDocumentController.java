package com.iclinical.technology.documents;

import com.iclinical.technology.documents.dto.ClinicalDocumentCreateRequest;
import com.iclinical.technology.documents.dto.ClinicalDocumentResponse;
import com.iclinical.technology.documents.dto.ClinicalDocumentSummaryResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ClinicalDocumentController {

    private final ClinicalDocumentService documentService;

    public ClinicalDocumentController(ClinicalDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/clinical-documents")
    public List<ClinicalDocumentSummaryResponse> list(
        @RequestParam(required = false) UUID patientId,
        @RequestParam(required = false) String documentType
    ) {
        return documentService.list(patientId, documentType);
    }

    @GetMapping("/clinical-documents/{id}")
    public ClinicalDocumentResponse getById(@PathVariable UUID id) {
        return documentService.getById(id);
    }

    @GetMapping("/patients/{patientId}/documents")
    public List<ClinicalDocumentSummaryResponse> listByPatient(@PathVariable UUID patientId) {
        return documentService.listByPatient(patientId);
    }

    @PostMapping(value = "/clinical-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalDocumentResponse create(
        @RequestParam UUID patientId,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam String documentType,
        @RequestParam String title,
        @RequestParam(required = false) String description,
        @RequestParam MultipartFile file
    ) {
        return documentService.create(new ClinicalDocumentCreateRequest(patientId, professionalId, documentType, title, description, file));
    }

    @GetMapping("/clinical-documents/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        var download = documentService.download(id);
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(download.fileName()).build());
        headers.setContentLength(download.fileSize());
        headers.setContentType(MediaType.parseMediaType(download.mimeType()));
        return new ResponseEntity<>(download.resource(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/clinical-documents/{id}")
    public ClinicalDocumentResponse delete(@PathVariable UUID id) {
        return documentService.delete(id);
    }
}
