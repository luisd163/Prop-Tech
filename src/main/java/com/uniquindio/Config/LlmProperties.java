package com.uniquindio.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proptech.llm")
public class LlmProperties {

    /** openai | ollama */
    private String provider = "openai";

    private String apiKey = "";

    /** OpenAI: https://api.openai.com/v1 — Ollama: http://localhost:11434/v1 */
    private String baseUrl = "https://api.openai.com/v1";

    private String model = "gpt-4o-mini";

    private boolean enabled = true;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean tieneCredenciales() {
        if (!enabled) {
            return false;
        }
        if ("ollama".equalsIgnoreCase(provider)) {
            return baseUrl != null && !baseUrl.isBlank();
        }
        return apiKey != null && !apiKey.isBlank();
    }
}
