package com.uniquindio.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.uniquindio.Config.LlmProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LlmChatService {

    private static final String SYSTEM_PROMPT = """
            Eres el asistente virtual de PropTech, una inmobiliaria.
            Responde en español, de forma breve y amable.
            REGLAS OBLIGATORIAS:
            - Usa ÚNICAMENTE los datos del CONTEXTO para precios, códigos, zonas y disponibilidad.
            - Si el usuario pregunta por un inmueble que no está en el contexto, indica que no tienes ese dato y sugiere revisar el catálogo.
            - NUNCA inventes precios ni direcciones.
            - Puedes explicar visitas, recomendaciones y zonas según el contexto.
            """;

    private final LlmProperties properties;
    private final ChatContextService contextService;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public LlmChatService(LlmProperties properties, ChatContextService contextService) {
        this.properties = properties;
        this.contextService = contextService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public ChatResult responder(String idCliente, String mensajeUsuario, List<Map<String, String>> historial) {
        if (idCliente == null || idCliente.isBlank()) {
            return ChatResult.error("Debes iniciar sesión como cliente.");
        }
        if (mensajeUsuario == null || mensajeUsuario.isBlank()) {
            return ChatResult.error("Escribe un mensaje para el asistente.");
        }

        if (!properties.isEnabled()) {
            return ChatResult.error("El asistente está desactivado en la configuración.");
        }

        if (!properties.tieneCredenciales()) {
            return ChatResult.error(properties.mensajeConfiguracion());
        }

        String contexto = contextService.construirContextoRag(idCliente);

        try {
            String respuesta = llamarLlm(contexto, mensajeUsuario.trim(), historial);
            return ChatResult.ok(respuesta);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            if (msg.startsWith("Tu cuenta") || msg.startsWith("API key") || msg.startsWith("Error del proveedor")) {
                return ChatResult.error(msg);
            }
            return ChatResult.error("No se pudo contactar al modelo: " + msg);
        }
    }

    private String llamarLlm(String contexto, String mensajeUsuario, List<Map<String, String>> historial)
            throws Exception {

        JsonArray messages = new JsonArray();

        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", SYSTEM_PROMPT + "\n\nCONTEXTO:\n" + contexto);
        messages.add(system);

        if (historial != null) {
            int desde = Math.max(0, historial.size() - 6);
            for (int i = desde; i < historial.size(); i++) {
                Map<String, String> turno = historial.get(i);
                String role = turno.get("role");
                String content = turno.get("content");
                if (role == null || content == null || content.isBlank()) {
                    continue;
                }
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    continue;
                }
                JsonObject msg = new JsonObject();
                msg.addProperty("role", role);
                msg.addProperty("content", content);
                messages.add(msg);
            }
        }

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", mensajeUsuario);
        messages.add(user);

        JsonObject body = new JsonObject();
        body.addProperty("model", properties.getModel());
        body.add("messages", messages);
        body.addProperty("temperature", 0.3);
        body.addProperty("max_tokens", properties.getMaxTokens());

        String url = properties.getBaseUrl().replaceAll("/$", "") + "/chat/completions";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)));

        if (!"ollama".equalsIgnoreCase(properties.getProvider())) {
            builder.header("Authorization", "Bearer " + properties.getApiKey());
        } else if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + properties.getApiKey());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(mensajeErrorApi(response.statusCode(), response.body()));
        }

        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
            JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (choice.has("message")) {
                return choice.getAsJsonObject("message").get("content").getAsString().trim();
            }
        }

        throw new IllegalStateException("Respuesta del modelo sin contenido.");
    }

    private String mensajeErrorApi(int status, String body) {
        String tipo = "";
        String detalle = "";
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("error")) {
                JsonObject err = json.getAsJsonObject("error");
                if (err.has("type")) {
                    tipo = err.get("type").getAsString();
                }
                if (err.has("message")) {
                    detalle = err.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
            detalle = recortar(body, 120);
        }

        if (status == 429 || "insufficient_quota".equals(tipo)) {
            return "Tu cuenta de OpenAI no tiene crédito o cuota disponible (HTTP 429). "
                    + "Entra a https://platform.openai.com/settings/organization/billing "
                    + "y agrega un método de pago o recarga saldo. "
                    + "También puedes usar Ollama gratis (ver application-local.properties.example).";
        }
        if (status == 401) {
            return "API key inválida o revocada (HTTP 401). Revisa application-local.properties.";
        }
        if (status == 403) {
            return "Acceso denegado por OpenAI (HTTP 403). " + recortar(detalle, 100);
        }
        return "Error del proveedor (HTTP " + status + "): " + recortar(detalle, 150);
    }

    private String recortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= max ? texto : texto.substring(0, max) + "...";
    }

    public record ChatResult(boolean exito, String mensaje) {
        public static ChatResult ok(String mensaje) {
            return new ChatResult(true, mensaje);
        }

        public static ChatResult error(String mensaje) {
            return new ChatResult(false, mensaje);
        }
    }
}
