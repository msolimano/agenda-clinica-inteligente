package com.iclinical.technology.clinicalinsights;

import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightResponse;
import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightReviewRequest;
import com.iclinical.technology.clinicalinsights.dto.ClinicalInsightSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ClinicalInsightController {

    private final ClinicalInsightService insightService;

    public ClinicalInsightController(ClinicalInsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/clinical-insights")
    public List<ClinicalInsightSummaryResponse> list(
        @RequestParam(required = false) UUID patientId,
        @RequestParam(required = false) UUID documentId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false, name = "type") String type
    ) {
        return insightService.list(patientId, documentId, status, type);
    }

    @GetMapping("/clinical-insights/{id}")
    public ClinicalInsightResponse getById(@PathVariable UUID id) {
        return insightService.getById(id);
    }

    @GetMapping("/patients/{patientId}/clinical-insights")
    public List<ClinicalInsightSummaryResponse> listByPatient(@PathVariable UUID patientId) {
        return insightService.listByPatient(patientId);
    }

    @GetMapping("/clinical-documents/{documentId}/insights")
    public List<ClinicalInsightSummaryResponse> listByDocument(@PathVariable UUID documentId) {
        return insightService.listByDocument(documentId);
    }

    @PostMapping("/clinical-insights/{id}/accept")
    public ClinicalInsightResponse accept(@PathVariable UUID id, @RequestBody(required = false) ClinicalInsightReviewRequest request) {
        return insightService.accept(id, request);
    }

    @PostMapping("/clinical-insights/{id}/reject")
    public ClinicalInsightResponse reject(@PathVariable UUID id, @RequestBody(required = false) ClinicalInsightReviewRequest request) {
        return insightService.reject(id, request);
    }

    @PostMapping("/clinical-insights/{id}/dismiss")
    public ClinicalInsightResponse dismiss(@PathVariable UUID id, @RequestBody(required = false) ClinicalInsightReviewRequest request) {
        return insightService.dismiss(id, request);
    }
}
