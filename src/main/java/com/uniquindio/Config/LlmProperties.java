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

    private int maxTokens = 600;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        if (apiKey == null) {
            return "";
        }
        return apiKey.trim();
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

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens > 0 ? maxTokens : 600;
    }

    public boolean tieneCredenciales() {
        if (!enabled) {
            return false;
        }
        if ("ollama".equalsIgnoreCase(provider)) {
            return baseUrl != null && !baseUrl.isBlank();
        }
        String key = getApiKey();
        if (key.isBlank() || key.length() < 20) {
            return false;
        }
        if (key.startsWith("PEGA_") || key.startsWith("key_") || key.contains("TU_CLAVE")
                || key.contains("secret-key")) {
            return false;
        }
        return key.startsWith("sk-");
    }

    /** Listo para atender mensajes en la UI. */
    public boolean estaOperativo() {
        return tieneCredenciales();
    }

    public String mensajeConfiguracion() {
        if (!enabled) {
            return "El asistente está desactivado (proptech.llm.enabled=false).";
        }
        if ("ollama".equalsIgnoreCase(provider)) {
            return "Configura Ollama: ejecuta 'ollama serve' y el modelo "
                    + model + " en " + baseUrl;
        }
        return "Configura tu API key: copia application-local.properties.example "
                + "a application-local.properties o define OPENAI_API_KEY.";
    }
}
