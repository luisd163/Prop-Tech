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

    @GetMapping({"/locaciones-cercanas", "/grafo"})
    public String locacionesCercanas(Model model, HttpSession session) {

        String idCliente = obtenerIdClienteSesion(session);

        if (idCliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("tituloPagina", "Locaciones cercanas");
        model.addAttribute("subtituloPagina",
                "Todos los inmuebles y zonas registrados en el sistema");
        model.addAttribute("paginaActual", "grafo");

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

        try {
            GrafoService.GrafoDTO grafo = grafoService.obtenerLocacionesCercanas(idCliente, 0);

            String grafoJson = gson.toJson(grafo);

            long totalNodos = grafo.getNodos().size();
            long totalAristas = grafo.getAristas().size();
            long totalZonas = grafo.getNodos().stream()
                    .filter(n -> "zona".equals(n.getTipo())).count();
            long totalDisponibles = grafo.getNodos().stream()
                    .filter(n -> "disp".equals(n.getTipo())).count();
            long totalReservados = grafo.getNodos().stream()
                    .filter(n -> "reservado".equals(n.getTipo())).count();

            model.addAttribute("grafoJson", grafoJson);
            model.addAttribute("totalNodos", totalNodos);
            model.addAttribute("totalAristas", totalAristas);
            model.addAttribute("totalZonas", totalZonas);
            model.addAttribute("totalDisponibles", totalDisponibles);
            model.addAttribute("totalReservados", totalReservados);

        } catch (Exception e) {
            model.addAttribute("grafoJson", "{}");
            model.addAttribute("totalNodos", 0);
            model.addAttribute("totalAristas", 0);
            model.addAttribute("totalZonas", 0);
            model.addAttribute("totalDisponibles", 0);
            model.addAttribute("totalReservados", 0);
            model.addAttribute("error", "No se pudo cargar el grafo: " + e.getMessage());
            e.printStackTrace();
        }

        return "Grafo";
    }

    @GetMapping("/api/grafo/locaciones")
    @ResponseBody
    public ResponseEntity<GrafoService.GrafoDTO> obtenerGrafo(HttpSession session) {

        String idCliente = obtenerIdClienteSesion(session);
        if (idCliente == null) {
            return ResponseEntity.status(401).build();
        }

        GrafoService.GrafoDTO grafo = grafoService.obtenerLocacionesCercanas(idCliente, 0);
        return ResponseEntity.ok(grafo);
    }

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

    @PostMapping("/api/grafo/registrar-visita")
    @ResponseBody
    public ResponseEntity<Void> registrarVisita(
            @RequestParam String idCliente,
            @RequestParam String codigoInmueble) {

        grafoService.registrarVisita(idCliente, codigoInmueble);
        return ResponseEntity.ok().build();
    }

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
