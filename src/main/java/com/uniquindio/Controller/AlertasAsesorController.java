package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.uniquindio.Service.VisitaService;
import com.uniquindio.Model.Visita;

@Controller
public class AlertasAsesorController {

    @GetMapping("/alertas-asesor")
    public String showAlertasAsesor(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam(name = "q", required = false) String query,
            Model model) {

        if (asesor == null) {
            return "redirect:/login";
        }

        InmuebleService inmuebleService = new InmuebleService();
        List<Inmueble> inmuebles = inmuebleService.obtenerInmueblesPorAsesor(asesor.getIdentificacion());

        String qBusqueda = (query == null) ? "" : query.trim().toLowerCase();

        // obtener inmuebles que ya tienen visitas asignadas para este asesor
        VisitaService visitaService = new VisitaService();
        List<Visita> visitas = visitaService.obtenerVisitasPorAsesor(asesor.getIdentificacion());
        Set<String> inmueblesConVisita = visitas.stream()
            .filter(Objects::nonNull)
            .map(Visita::getInmueble)
            .filter(Objects::nonNull)
            .map(i -> i.getCodigo())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // filtrar inmuebles por búsqueda y excluir los que ya tienen visitas
        List<Inmueble> inmueblesFiltrados = inmuebles.stream()
            .filter(Objects::nonNull)
            .filter(inmueble -> !inmueblesConVisita.contains(inmueble.getCodigo()))
            .filter(inmueble -> {
                if (qBusqueda.isEmpty()) return true;
                String nombre = inmueble.getNombre() != null ? inmueble.getNombre().toLowerCase() : "";
                String direccion = inmueble.getDireccion() != null ? inmueble.getDireccion().toLowerCase() : "";
                String ciudad = inmueble.getCiudad() != null ? inmueble.getCiudad().toLowerCase() : "";
                String barrio = inmueble.getBarrio() != null ? inmueble.getBarrio().toLowerCase() : "";
                return nombre.contains(qBusqueda) || direccion.contains(qBusqueda) || ciudad.contains(qBusqueda) || barrio.contains(qBusqueda);
            })
            .collect(Collectors.toList());

        model.addAttribute("inmuebles", inmueblesFiltrados);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", asesor.getEspecialidad());
        model.addAttribute("inicialesAsesor", asesor.getNombre().substring(0, 1).toUpperCase());

        return "alertas-asesor";
    }
}
