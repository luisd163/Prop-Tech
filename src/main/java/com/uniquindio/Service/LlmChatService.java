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

        String contexto = contextService.construirContextoRag(idCliente);

        if (!properties.tieneCredenciales()) {
            return ChatResult.error(
                    "El asistente LLM no está configurado. Define OPENAI_API_KEY o usa Ollama "
                            + "(proptech.llm.provider=ollama) en application.properties."
            );
        }

        try {
            String respuesta = llamarLlm(contexto, mensajeUsuario.trim(), historial);
            return ChatResult.ok(respuesta);
        } catch (Exception e) {
            return ChatResult.error("No se pudo contactar al modelo: " + e.getMessage());
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
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + recortar(response.body(), 300));
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
