package com.iclinical.technology.ai;

import com.iclinical.technology.ai.dto.DocumentAIProviderStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/providers")
public class DocumentAIProviderController {

    private final DocumentAIProviderService providerService;

    public DocumentAIProviderController(DocumentAIProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/status")
    public DocumentAIProviderStatusResponse status() {
        return providerService.getStatus();
    }
}
