package com.uniquindio.Controller;

import com.uniquindio.Config.LlmProperties;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Service.LlmChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final LlmChatService chatService;
    private final LlmProperties llmProperties;

    public ChatController(LlmChatService chatService, LlmProperties llmProperties) {
        this.chatService = chatService;
        this.llmProperties = llmProperties;
    }

    @PostMapping("/mensaje")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestBody ChatRequest request,
            HttpSession session) {

        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "exito", false,
                    "error", "Sesión de cliente requerida"
            ));
        }

        List<Map<String, String>> historial = request.historial() != null
                ? request.historial()
                : new ArrayList<>();

        LlmChatService.ChatResult result = chatService.responder(
                idCliente,
                request.mensaje(),
                historial
        );

        if (result.exito()) {
            return ResponseEntity.ok(Map.of(
                    "exito", true,
                    "respuesta", result.mensaje()
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "exito", false,
                "error", result.mensaje()
        ));
    }

    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> estado(HttpSession session) {
        boolean sesionCliente = obtenerIdClienteSesion(session) != null;
        boolean operativo = llmProperties.estaOperativo();

        return ResponseEntity.ok(Map.of(
                "disponible", operativo && sesionCliente,
                "configurado", operativo,
                "habilitado", llmProperties.isEnabled(),
                "provider", llmProperties.getProvider() != null ? llmProperties.getProvider() : "openai",
                "model", llmProperties.getModel() != null ? llmProperties.getModel() : "",
                "sesionCliente", sesionCliente,
                "ayuda", operativo
                        ? (sesionCliente ? "Listo para chatear." : "Inicia sesión como cliente.")
                        : llmProperties.mensajeConfiguracion()
        ));
    }

    private String obtenerIdClienteSesion(HttpSession session) {
        Object idDirecto = session.getAttribute("clienteId");
        if (idDirecto instanceof String id && !id.isBlank()) {
            return id;
        }

        Object clienteSesion = session.getAttribute("clienteSesion");
        if (clienteSesion instanceof Cliente cliente) {
            String identificacion = cliente.getIdentificacion();
            return (identificacion == null || identificacion.isBlank()) ? null : identificacion;
        }

        return null;
    }

    public record ChatRequest(String mensaje, List<Map<String, String>> historial) {}
}
