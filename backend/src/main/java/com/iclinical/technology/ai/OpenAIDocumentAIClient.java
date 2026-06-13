package com.iclinical.technology.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iclinical.technology.documents.ClinicalDocument;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class OpenAIDocumentAIClient implements DocumentAIClient {

    private static final String PROVIDER = "openai";
    private final DocumentAIProperties properties;
    private final ObjectMapper objectMapper;

    OpenAIDocumentAIClient(DocumentAIProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public DocumentAIResult analyze(DocumentAIRequest request) {
        var started = System.nanoTime();
        var document = request.document();
        validateConfiguration(started);
        validateDocument(document, started);

        var client = RestClient.builder()
            .baseUrl(baseUrl())
            .requestFactory(requestFactory())
            .build();

        try {
            var response = client.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                .body(requestBody(request))
                .retrieve()
                .body(JsonNode.class);

            if (response == null) {
                throw providerException("OpenAI no devolvio respuesta para el analisis", "empty_response", started, Map.of("httpStatus", "empty"));
            }

            var payload = extractOutputText(response);
            if (!StringUtils.hasText(payload)) {
                throw providerException("OpenAI no devolvio una salida estructurada valida", "missing_output_text", started, limitedMetadata(response));
            }

            var result = objectMapper.readValue(payload, OpenAIClinicalAnalysis.class);
            var usage = response.path("usage");
            var latencyMs = elapsedMs(started);

            return new DocumentAIResult(
                PROVIDER,
                nullableText(response.path("id")),
                properties.getPromptVersion(),
                nullableInt(usage.path("input_tokens")),
                nullableInt(usage.path("output_tokens")),
                nullableInt(usage.path("total_tokens")),
                latencyMs,
                null,
                Map.of("endpoint", "responses", "structuredOutput", true),
                properties.getOpenai().getModel(),
                nullToFallback(result.clinical_summary(), "Sin resumen clinico disponible."),
                listOrEmpty(result.relevant_findings()),
                listOrEmpty(result.mentioned_diagnoses()),
                listOrEmpty(result.mentioned_medications()),
                listOrEmpty(result.mentioned_allergies()),
                nullToFallback(result.recommendations(), "Revisar el documento original y validar los hallazgos con un profesional de salud."),
                insightListOrEmpty(result.insights())
            );
        } catch (RestClientResponseException exception) {
            throw providerException(
                "OpenAI no pudo completar el analisis documental",
                errorCode(exception),
                started,
                Map.of("httpStatus", exception.getStatusCode().value())
            );
        } catch (DocumentAIProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw providerException("No fue posible interpretar la respuesta de OpenAI", "invalid_openai_response", started, Map.of("errorType", exception.getClass().getSimpleName()));
        } catch (Exception exception) {
            throw providerException("No fue posible interpretar la respuesta de OpenAI", "invalid_openai_response", started, Map.of("errorType", exception.getClass().getSimpleName()));
        }
    }

    @Override
    public DocumentAIProviderStatus status() {
        var configured = properties.isOpenAIConfigured();
        var message = configured ? "Proveedor OpenAI configurado" : "Proveedor OpenAI seleccionado sin API key o modelo configurado";
        return new DocumentAIProviderStatus(PROVIDER, configured, configured ? properties.getOpenai().getModel() : null, properties.getPromptVersion(), true, configured, message);
    }

    private void validateConfiguration(long started) {
        if (!properties.isOpenAIConfigured()) {
            throw providerException("Proveedor OpenAI no configurado: defina OPENAI_API_KEY y OPENAI_DOCUMENT_MODEL", "openai_not_configured", started, Map.of("missingConfiguration", true));
        }
    }

    private void validateDocument(ClinicalDocument document, long started) {
        var fileSize = document.getFileSizeBytes() == null ? 0L : document.getFileSizeBytes();
        if (fileSize > properties.getMaxInputBytes()) {
            throw providerException("Documento excede el tamano maximo configurado para analisis IA", "document_ai_input_too_large", started, Map.of("maxInputBytes", properties.getMaxInputBytes()));
        }
    }

    private Map<String, Object> requestBody(DocumentAIRequest request) {
        var document = request.document();
        var extraction = request.textExtraction();
        var extractedText = request.extractedText();
        var extractionStatus = extraction == null ? "not_requested" : extraction.status();
        var extractionMessage = extraction == null ? null : extraction.errorMessage();
        var userPrompt = "Analiza exclusivamente la informacion presente en el documento clinico. "
            + "No infieras datos no presentes. No crees diagnosticos, prescripciones, tratamientos ni modificaciones automaticas de ficha clinica. "
            + "Titulo: " + clean(document.getTitle()) + ". Tipo: " + clean(document.getDocumentType()) + ". Archivo: " + clean(document.getOriginalFilename())
            + ". MIME: " + clean(document.getMimeType()) + ". Tamano bytes: " + (document.getFileSizeBytes() == null ? 0 : document.getFileSizeBytes()) + ". "
            + "Estado de extraccion de texto: " + clean(extractionStatus) + ". "
            + (StringUtils.hasText(extractionMessage) ? "Mensaje de extraccion: " + clean(extractionMessage) + ". " : "")
            + "Texto extraido para analisis: " + extractedTextForPrompt(extractedText) + ".";

        return Map.of(
            "model", properties.getOpenai().getModel(),
            "input", List.of(
                Map.of(
                    "role", "system",
                    "content", List.of(Map.of("type", "input_text", "text", systemPrompt()))
                ),
                Map.of(
                    "role", "user",
                    "content", List.of(Map.of("type", "input_text", "text", userPrompt))
                )
            ),
            "text", Map.of("format", Map.of(
                "type", "json_schema",
                "name", "clinical_document_analysis",
                "strict", true,
                "schema", responseSchema()
            ))
        );
    }

    private String systemPrompt() {
        return "Eres un asistente clinico documental para I-Clinical Technology. Devuelve solo JSON valido segun el esquema. "
            + "No entregues diagnostico definitivo, no indiques tratamiento automatico y no inventes informacion. "
            + "Toda sugerencia debe quedar pendiente de revision profesional. "
            + "Todo resultado debe ser revisado por un profesional de salud.";
    }

    private String extractedTextForPrompt(String text) {
        if (!StringUtils.hasText(text)) {
            return "No fue posible extraer texto clinico estructurado del documento.";
        }
        var cleaned = clean(text);
        var maxLength = 12_000;
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private Map<String, Object> responseSchema() {
        var propertiesMap = new LinkedHashMap<String, Object>();
        propertiesMap.put("clinical_summary", Map.of("type", "string"));
        propertiesMap.put("relevant_findings", Map.of("type", "array", "items", Map.of("type", "string")));
        propertiesMap.put("mentioned_diagnoses", Map.of("type", "array", "items", Map.of("type", "string")));
        propertiesMap.put("mentioned_medications", Map.of("type", "array", "items", Map.of("type", "string")));
        propertiesMap.put("mentioned_allergies", Map.of("type", "array", "items", Map.of("type", "string")));
        propertiesMap.put("recommendations", Map.of("type", "string"));
        propertiesMap.put("insights", Map.of("type", "array", "items", insightSchema()));

        return Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("clinical_summary", "relevant_findings", "mentioned_diagnoses", "mentioned_medications", "mentioned_allergies", "recommendations", "insights"),
            "properties", propertiesMap
        );
    }

    private Map<String, Object> insightSchema() {
        var propertiesMap = new LinkedHashMap<String, Object>();
        propertiesMap.put("type", Map.of(
            "type", "string",
            "enum", List.of("clinical_summary", "clinical_alert", "diagnosis_candidate", "medication_candidate", "allergy_candidate", "risk_factor_candidate", "lab_result_candidate", "observation_candidate")
        ));
        propertiesMap.put("title", Map.of("type", "string"));
        propertiesMap.put("description", Map.of("type", "string"));
        propertiesMap.put("source_text", Map.of("type", List.of("string", "null")));
        propertiesMap.put("confidence", Map.of("type", List.of("number", "null"), "minimum", 0, "maximum", 1));

        return Map.of(
            "type", "object",
            "additionalProperties", false,
            "required", List.of("type", "title", "description", "source_text", "confidence"),
            "properties", propertiesMap
        );
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        var timeout = Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds()));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private String baseUrl() {
        var configured = properties.getOpenai().getBaseUrl();
        var baseUrl = StringUtils.hasText(configured) ? configured.trim() : "https://api.openai.com/v1";
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String extractOutputText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.hasNonNull("output_text")) {
            return node.path("output_text").asText();
        }
        if (node.hasNonNull("text") && node.path("type").asText().equals("output_text")) {
            return node.path("text").asText();
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                var value = extractOutputText(child);
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        } else if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var value = extractOutputText(fields.next().getValue());
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private DocumentAIProviderException providerException(String message, String code, long started, Map<String, Object> metadata) {
        return new DocumentAIProviderException(message, PROVIDER, properties.getOpenai().getModel(), properties.getPromptVersion(), code, elapsedMs(started), metadata);
    }

    private String errorCode(RestClientResponseException exception) {
        try {
            var body = objectMapper.readTree(exception.getResponseBodyAsString());
            var code = body.path("error").path("code").asText(null);
            if (StringUtils.hasText(code)) {
                return code;
            }
            var type = body.path("error").path("type").asText(null);
            if (StringUtils.hasText(type)) {
                return type;
            }
        } catch (Exception ignored) {
            // Intentionally avoid logging provider payloads because they may contain clinical data.
        }
        return "openai_http_" + exception.getStatusCode().value();
    }

    private Map<String, Object> limitedMetadata(JsonNode response) {
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("endpoint", "responses");
        if (response != null && response.hasNonNull("id")) {
            metadata.put("providerRequestId", response.path("id").asText());
        }
        if (response != null && response.hasNonNull("status")) {
            metadata.put("providerStatus", response.path("status").asText());
        }
        return metadata;
    }

    private Integer nullableInt(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private String nullableText(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private int elapsedMs(long started) {
        return Math.toIntExact((System.nanoTime() - started) / 1_000_000L);
    }

    private List<String> listOrEmpty(List<String> value) {
        return value == null ? List.of() : new ArrayList<>(value);
    }

    private List<DocumentAIInsight> insightListOrEmpty(List<OpenAIClinicalInsight> value) {
        if (value == null) {
            return List.of();
        }
        return value.stream()
            .map(item -> new DocumentAIInsight(item.type(), item.title(), item.description(), item.source_text(), item.confidence()))
            .toList();
    }

    private String nullToFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String clean(String value) {
        return StringUtils.hasText(value) ? value.trim() : "no informado";
    }

    private record OpenAIClinicalAnalysis(
        String clinical_summary,
        List<String> relevant_findings,
        List<String> mentioned_diagnoses,
        List<String> mentioned_medications,
        List<String> mentioned_allergies,
        String recommendations,
        List<OpenAIClinicalInsight> insights
    ) {
    }

    private record OpenAIClinicalInsight(
        String type,
        String title,
        String description,
        String source_text,
        Double confidence
    ) {
    }
}
