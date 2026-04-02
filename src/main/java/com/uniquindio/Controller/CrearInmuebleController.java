package com.uniquindio.Controller;

import com.uniquindio.Model.Inmueble.Disponibilidad;
import com.uniquindio.Model.Inmueble.EstadoInmueble;
import com.uniquindio.Model.Inmueble.Finalidad;
import com.uniquindio.Model.Inmueble.TipoInmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CrearInmuebleController {

    private final InmuebleService inmuebleService;

    public CrearInmuebleController() {
        this.inmuebleService = new InmuebleService();
    }

    @GetMapping("/inmuebles/crear")
    public String showCrearInmueble(Model model) {
        cargarCombos(model);
        model.addAttribute("titulo", "Crear Inmueble");
        return "crear-inmueble";
    }

    @PostMapping("/inmuebles/crear")
    public String processCrearInmueble(
            @RequestParam String codigo,
            @RequestParam String direccion,
            @RequestParam String ciudad,
            @RequestParam String barrio,
            @RequestParam String asesorResponsableString,
            @RequestParam TipoInmueble tipoInmueble,
            @RequestParam Finalidad finalidad,
            @RequestParam double precio,
            @RequestParam double area,
            @RequestParam int numeroHabitaciones,
            @RequestParam int numeroBanos,
            @RequestParam EstadoInmueble estadoInmueble,
            @RequestParam Disponibilidad disponibilidad,
            Model model
    ) {
        try {
            inmuebleService.registrarInmueble(
                    codigo,
                    direccion,
                    ciudad,
                    barrio,
                    asesorResponsableString,
                    tipoInmueble,
                    finalidad,
                    precio,
                    area,
                    numeroHabitaciones,
                    numeroBanos,
                    estadoInmueble,
                    disponibilidad
            );
            model.addAttribute("mensaje", "Inmueble registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("codigo", codigo);
            model.addAttribute("direccion", direccion);
            model.addAttribute("ciudad", ciudad);
            model.addAttribute("barrio", barrio);
            model.addAttribute("asesorResponsableString", asesorResponsableString);
            model.addAttribute("precio", precio);
            model.addAttribute("area", area);
            model.addAttribute("numeroHabitaciones", numeroHabitaciones);
            model.addAttribute("numeroBanos", numeroBanos);
        }

        cargarCombos(model);
        model.addAttribute("titulo", "Crear Inmueble");
        return "crear-inmueble";
    }

    private void cargarCombos(Model model) {
        model.addAttribute("tiposInmueble", TipoInmueble.values());
        model.addAttribute("finalidades", Finalidad.values());
        model.addAttribute("estadosInmueble", EstadoInmueble.values());
        model.addAttribute("disponibilidades", Disponibilidad.values());
    }
}
