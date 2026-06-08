package com.iclinical.technology.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class DocumentAIProperties {

    private String provider = "mock";
    private String promptVersion = "DOCUMENT-AI-V1";
    private int timeoutSeconds = 45;
    private long maxInputBytes = 10_485_760L;
    private OpenAI openai = new OpenAI();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public long getMaxInputBytes() { return maxInputBytes; }
    public void setMaxInputBytes(long maxInputBytes) { this.maxInputBytes = maxInputBytes; }
    public OpenAI getOpenai() { return openai; }
    public void setOpenai(OpenAI openai) { this.openai = openai; }

    public String resolvedProvider() {
        return StringUtils.hasText(provider) ? provider.trim().toLowerCase() : "mock";
    }

    public boolean isOpenAISelected() {
        return "openai".equals(resolvedProvider());
    }

    public boolean isOpenAIConfigured() {
        return StringUtils.hasText(openai.getApiKey()) && StringUtils.hasText(openai.getModel());
    }

    public static class OpenAI {
        private String apiKey = "";
        private String model = "";
        private String baseUrl = "https://api.openai.com/v1";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
