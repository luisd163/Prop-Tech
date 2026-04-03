package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Inmueble.Disponibilidad;
import com.uniquindio.Model.Inmueble.Finalidad;
import com.uniquindio.Model.Inmueble.TipoInmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

@Controller
public class InmueblesAsesorController {

    @GetMapping("/inmuebles/todos")
    public String showInmuebles(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam(name = "codigo", required = false) String codigo,
            @RequestParam(name = "tipoInmueble", required = false) String tipoInmueble,
            @RequestParam(name = "finalidad", required = false) String finalidad,
            @RequestParam(name = "disponibilidad", required = false) String disponibilidad,
            Model model) {

        if (asesor == null) {
            return "redirect:/login";
        }

        InmuebleService inmuebleService = new InmuebleService();
        List<Inmueble> todosInmueblesAsesor = inmuebleService.obtenerInmueblesPorAsesor(asesor.getIdentificacion());

        List<Inmueble> inmueblesAsesor = new ArrayList<>(todosInmueblesAsesor);
        String codigoBusqueda = (codigo == null) ? "" : codigo.trim();
        String tipoFiltro = (tipoInmueble == null) ? "" : tipoInmueble.trim();
        String finalidadFiltro = (finalidad == null) ? "" : finalidad.trim();
        String disponibilidadFiltro = (disponibilidad == null) ? "" : disponibilidad.trim();

        if (!codigoBusqueda.isEmpty()) {
            Inmueble inmuebleEncontrado = inmuebleService.obtenerInmueble(codigoBusqueda);
            inmueblesAsesor = new ArrayList<>();

            if (inmuebleEncontrado != null
                    && asesor.getIdentificacion().equals(inmuebleEncontrado.getCodigoAsesorResponsable())) {
                inmueblesAsesor.add(inmuebleEncontrado);
            }
        }

        if (!tipoFiltro.isEmpty()) {
            try {
                TipoInmueble tipoEnum = TipoInmueble.valueOf(tipoFiltro);
                inmueblesAsesor.removeIf(inmueble -> inmueble == null || inmueble.getTipoInmueble() != tipoEnum);
            } catch (IllegalArgumentException ignored) {
                // Si el valor no es válido, no aplica filtro
            }
        }

        if (!finalidadFiltro.isEmpty()) {
            try {
                Finalidad finalidadEnum = Finalidad.valueOf(finalidadFiltro);
                inmueblesAsesor.removeIf(inmueble -> inmueble == null || inmueble.getFinalidad() != finalidadEnum);
            } catch (IllegalArgumentException ignored) {
                // Si el valor no es válido, no aplica filtro
            }
        }

        if (!disponibilidadFiltro.isEmpty()) {
            try {
                Disponibilidad disponibilidadEnum = Disponibilidad.valueOf(disponibilidadFiltro);
                inmueblesAsesor.removeIf(inmueble -> inmueble == null || inmueble.getDisponibilidad() != disponibilidadEnum);
            } catch (IllegalArgumentException ignored) {
                // Si el valor no es válido, no aplica filtro
            }
        }

        int cantidadInmuebles = todosInmueblesAsesor.size();
        int cantidadDisponibles = inmuebleService.cantInmueblesDisponibles(todosInmueblesAsesor);
        int cantidadNoDisponibles = inmuebleService.cantInmueblesNoDisponibles(todosInmueblesAsesor);
        int cantidadReservados = inmuebleService.cantInmueblesReservados(todosInmueblesAsesor);

        model.addAttribute("kpiTotalAsignados", cantidadInmuebles);
        model.addAttribute("kpiDisponibles", cantidadDisponibles);
        model.addAttribute("kpiNoDisponibles", cantidadNoDisponibles);
        model.addAttribute("kpiReservados", cantidadReservados);
        model.addAttribute("inmueblesAsesor", inmueblesAsesor);
        model.addAttribute("codigoBusqueda", codigoBusqueda);
        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("finalidadFiltro", finalidadFiltro);
        model.addAttribute("disponibilidadFiltro", disponibilidadFiltro);

        return "inmuebles-asesor";
    }
    
}
