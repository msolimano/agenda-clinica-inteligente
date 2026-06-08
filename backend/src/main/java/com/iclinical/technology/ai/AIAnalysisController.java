package com.iclinical.technology.ai;

import com.iclinical.technology.ai.dto.AIAnalysisResponse;
import com.iclinical.technology.ai.dto.AIAnalysisSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AIAnalysisController {

    private final AIAnalysisService analysisService;

    public AIAnalysisController(AIAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/clinical-documents/{id}/ai-analysis")
    @ResponseStatus(HttpStatus.CREATED)
    public AIAnalysisResponse requestDocumentAnalysis(@PathVariable UUID id) {
        return analysisService.requestAnalysis(id);
    }

    @GetMapping("/clinical-documents/{id}/ai-analysis")
    public List<AIAnalysisSummaryResponse> listDocumentAnalyses(@PathVariable UUID id) {
        return analysisService.listByDocument(id);
    }

    @GetMapping("/ai-analyses/{id}")
    public AIAnalysisResponse getById(@PathVariable UUID id) {
        return analysisService.getById(id);
    }

    @PostMapping("/ai-analyses/{id}/retry")
    @ResponseStatus(HttpStatus.CREATED)
    public AIAnalysisResponse retry(@PathVariable UUID id) {
        return analysisService.retry(id);
    }

    @GetMapping("/ai-analyses")
    public List<AIAnalysisSummaryResponse> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) UUID patientId,
        @RequestParam(required = false) UUID documentId
    ) {
        return analysisService.list(status, patientId, documentId);
    }
}
