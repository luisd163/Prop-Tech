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
import java.util.stream.Collectors;

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

        if (!qBusqueda.isEmpty()) {
            inmuebles = inmuebles.stream()
                    .filter(inmueble -> inmueble.getNombre().toLowerCase().contains(qBusqueda)
                            || (inmueble.getDireccion() != null && inmueble.getDireccion().toLowerCase().contains(qBusqueda))
                            || (inmueble.getCiudad() != null && inmueble.getCiudad().toLowerCase().contains(qBusqueda))
                            || (inmueble.getBarrio() != null && inmueble.getBarrio().toLowerCase().contains(qBusqueda)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("inmuebles", inmuebles);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", asesor.getEspecialidad());
        model.addAttribute("inicialesAsesor", asesor.getNombre().substring(0, 1).toUpperCase());

        return "alertas-asesor";
    }
}
