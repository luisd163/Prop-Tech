package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Visita;
import com.uniquindio.Service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
public class VisitasAsesorController {

    @GetMapping("/visitas")
    public String showVisitas(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "periodo", required = false) String periodo,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        VisitaService visitaService = new VisitaService();

        List<Visita> visitas = visitaService.obtenerVisitasPorAsesor(asesor.getIdentificacion());

        if (periodo != null && periodo.equalsIgnoreCase("semana")) {
            visitas = visitaService.filtrarEstaSemana(visitas);
        }

        if (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase("TODAS")) {
            try {
                Visita.EstadoVisita estadoEnum = Visita.EstadoVisita.valueOf(estado);
                visitas = visitaService.filtrarPorEstado(visitas, estadoEnum);
            } catch (IllegalArgumentException ignored) {
                // Ignora estado inválido
            }
        }

        visitas = visitaService.buscarPorClienteOInmueble(visitas, q);

        model.addAttribute("titulo", "Mis visitas");
        model.addAttribute("visitas", visitas);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("estadoFiltro", estado == null ? "TODAS" : estado);
        model.addAttribute("periodoFiltro", periodo == null ? "semana" : periodo);

        // KPIs del mes
        model.addAttribute("kpiTotalMes", visitaService.contarVisitasMes(asesor.getIdentificacion()));
        model.addAttribute("kpiPendientes", visitaService.contarPorEstado(asesor.getIdentificacion(), Visita.EstadoVisita.PENDIENTE));
        model.addAttribute("kpiConfirmadas", visitaService.contarPorEstado(asesor.getIdentificacion(), Visita.EstadoVisita.CONFIRMADA));
        model.addAttribute("kpiRealizadas", visitaService.contarPorEstado(asesor.getIdentificacion(), Visita.EstadoVisita.REALIZADA));
        model.addAttribute("kpiCanceladas", visitaService.contarPorEstado(asesor.getIdentificacion(), Visita.EstadoVisita.CANCELADA));

        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", "Asesor inmobiliario");
        model.addAttribute("inicialesAsesor", obtenerIniciales(asesor.getNombre()));

        return "visitas-asesor";
    }

    @PostMapping("/visitas/estado")
    public String actualizarEstado(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam String visitaId,
            @RequestParam String nuevoEstado,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "periodo", required = false) String periodo
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        VisitaService visitaService = new VisitaService();
        visitaService.actualizarEstado(visitaId, Visita.EstadoVisita.valueOf(nuevoEstado));

        StringBuilder redirect = new StringBuilder("redirect:/visitas");
        boolean hasParam = false;

        if (q != null && !q.isBlank()) {
            redirect.append(hasParam ? "&" : "?").append("q=").append(q);
            hasParam = true;
        }
        if (estado != null && !estado.isBlank()) {
            redirect.append(hasParam ? "&" : "?").append("estado=").append(estado);
            hasParam = true;
        }
        if (periodo != null && !periodo.isBlank()) {
            redirect.append(hasParam ? "&" : "?").append("periodo=").append(periodo);
        }

        return redirect.toString();
    }

    private String obtenerIniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "--";
        }

        String[] partes = nombreCompleto.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }

        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }
}
