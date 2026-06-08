package com.iclinical.technology.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class DocumentAIClientConfiguration {

    @Bean
    MockDocumentAIClient mockDocumentAIClient(DocumentAIProperties properties) {
        return new MockDocumentAIClient(properties);
    }

    @Bean
    OpenAIDocumentAIClient openAIDocumentAIClient(DocumentAIProperties properties, ObjectMapper objectMapper) {
        return new OpenAIDocumentAIClient(properties, objectMapper);
    }

    @Bean
    @Primary
    DocumentAIClient documentAIClient(DocumentAIProperties properties, MockDocumentAIClient mockClient, OpenAIDocumentAIClient openAIClient) {
        if (properties.isOpenAISelected()) {
            return openAIClient;
        }
        return mockClient;
    }
}
