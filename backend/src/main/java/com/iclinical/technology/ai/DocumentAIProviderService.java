package com.iclinical.technology.ai;

import com.iclinical.technology.ai.dto.DocumentAIProviderStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentAIProviderService {

    private final DocumentAIClient documentAIClient;

    public DocumentAIProviderService(DocumentAIClient documentAIClient) {
        this.documentAIClient = documentAIClient;
    }

    public DocumentAIProviderStatusResponse getStatus() {
        var status = documentAIClient.status();
        return new DocumentAIProviderStatusResponse(
            status.activeProvider(),
            status.configured(),
            status.modelName(),
            status.promptVersion(),
            status.mockAvailable(),
            status.openAIConfigured(),
            status.message()
        );
    }
}
