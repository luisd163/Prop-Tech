package com.uniquindio.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LlmStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(LlmStartupCheck.class);

    private final LlmProperties properties;

    public LlmStartupCheck(LlmProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verificarConfiguracion() {
        String key = properties.getApiKey();
        String masked = enmascarar(key);
        if (properties.estaOperativo()) {
            log.info("Asistente LLM listo | provider={} | model={} | api-key={}",
                    properties.getProvider(), properties.getModel(), masked);
        } else {
            log.warn("Asistente LLM NO configurado | api-key={} | {}", masked, properties.mensajeConfiguracion());
            if (key != null && (key.contains("PEGA_") || key.contains("TU_CLAVE"))) {
                log.warn("Parece que target/classes tiene un placeholder viejo. Ejecuta: mvnw clean spring-boot:run");
            }
        }
    }

    private String enmascarar(String key) {
        if (key == null || key.isBlank()) {
            return "(vacía)";
        }
        if (key.length() <= 10) {
            return "***";
        }
        return key.substring(0, 7) + "..." + key.substring(key.length() - 4);
    }
}
