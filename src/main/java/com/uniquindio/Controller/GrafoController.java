package com.uniquindio.Controller;

import com.google.gson.Gson;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Service.GrafoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class GrafoController {

    private final GrafoService grafoService = GrafoService.getInstancia();
    private final Gson gson = new Gson();

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. VISTA — Locaciones cercanas (carga la página Thymeleaf)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping({"/locaciones-cercanas", "/grafo"})
    public String locacionesCercanas(
            @RequestParam(defaultValue = "5.0") double radio,
            Model model,
            HttpSession session) {

        String idCliente = obtenerIdClienteSesion(session);

        if (idCliente == null) {
            return "redirect:/login";
        }

        try {
            // Obtener el grafo del servicio
            GrafoService.GrafoDTO grafo = grafoService.obtenerLocacionesCercanas(idCliente, radio);

            // Serializar a JSON para que el Canvas de la vista lo consuma
            String grafoJson = gson.toJson(grafo);
            System.out.println("DEBUG GrafoController: grafoJson=" + grafoJson);

            // Estadísticas para las tarjetas del panel derecho
            long totalNodos      = grafo.getNodos().size();
            long totalAristas    = grafo.getAristas().size();
            long totalZonas      = grafo.getNodos().stream()
                                        .filter(n -> "zona".equals(n.getTipo())).count();
            long totalDisponibles = grafo.getNodos().stream()
                                        .filter(n -> "disp".equals(n.getTipo())).count();

            model.addAttribute("grafoJson",        grafoJson);
            model.addAttribute("radioActual",       radio);
            model.addAttribute("totalNodos",        totalNodos);
            model.addAttribute("totalAristas",      totalAristas);
            model.addAttribute("totalZonas",        totalZonas);
            model.addAttribute("totalDisponibles",  totalDisponibles);
            model.addAttribute("paginaActual", "grafo");

            // Datos del cliente para el sidebar
            Cliente clienteSesion = obtenerClienteSesion(session);
            String nombreCliente = (String) session.getAttribute("nombreCliente");
            if ((nombreCliente == null || nombreCliente.isBlank()) && clienteSesion != null) {
                nombreCliente = clienteSesion.getNombre();
            }

            String inicialesCliente = (String) session.getAttribute("inicialesCliente");
            if ((inicialesCliente == null || inicialesCliente.isBlank()) && clienteSesion != null) {
                inicialesCliente = construirIniciales(clienteSesion.getNombre());
            }

            model.addAttribute("nombreCliente", nombreCliente != null ? nombreCliente : "Cliente");
            model.addAttribute("inicialesCliente", inicialesCliente != null ? inicialesCliente : "CL");
            model.addAttribute("rolCliente", "Cliente");

        } catch (Exception e) {
            model.addAttribute("grafoJson",  "{}");
            model.addAttribute("error", "No se pudo cargar el grafo: " + e.getMessage());
        }

        return "Grafo";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. API REST — Actualizar grafo al cambiar el radio (llamado desde JS)
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/grafo/locaciones")
    @ResponseBody
    public ResponseEntity<GrafoService.GrafoDTO> obtenerGrafo(
            @RequestParam(defaultValue = "5.0") double radio,
            HttpSession session) {

        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).build();
        }

        GrafoService.GrafoDTO grafo = grafoService.obtenerLocacionesCercanas(idCliente, radio);
        return ResponseEntity.ok(grafo);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. API REST — Recomendaciones basadas en el grafo
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/grafo/recomendaciones")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerRecomendaciones(HttpSession session) {
        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> recomendados = grafoService.recomendarPorComportamiento(idCliente);
        return ResponseEntity.ok(recomendados);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. API REST — Ranking de zonas por actividad
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/grafo/zonas-ranking")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> rankingZonas(HttpSession session) {
        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Integer> ranking = grafoService.rankingZonasPorActividad();
        return ResponseEntity.ok(ranking);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. API REST — Registrar visita en el grafo (llamado desde VisitaController)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/api/grafo/registrar-visita")
    @ResponseBody
    public ResponseEntity<Void> registrarVisita(
            @RequestParam String idCliente,
            @RequestParam String codigoInmueble) {

        grafoService.registrarVisita(idCliente, codigoInmueble);
        return ResponseEntity.ok().build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 6. API REST — Inmuebles visitados por un cliente
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/api/grafo/visitados")
    @ResponseBody
    public ResponseEntity<List<String>> inmueblesVisitados(HttpSession session) {
        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> visitados = grafoService.obtenerInmueblesVisitadosPor(idCliente);
        return ResponseEntity.ok(visitados);
    }

    private String obtenerIdClienteSesion(HttpSession session) {
        Object idDirecto = session.getAttribute("clienteId");
        if (idDirecto instanceof String id && !id.isBlank()) {
            return id;
        }

        Cliente cliente = obtenerClienteSesion(session);
        if (cliente != null) {
            String identificacion = cliente.getIdentificacion();
            return (identificacion == null || identificacion.isBlank()) ? null : identificacion;
        }

        return null;
    }

    private Cliente obtenerClienteSesion(HttpSession session) {
        Object clienteSesion = session.getAttribute("clienteSesion");
        if (clienteSesion instanceof Cliente cliente) {
            return cliente;
        }
        return null;
    }

    private String construirIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "CL";
        }
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }
}