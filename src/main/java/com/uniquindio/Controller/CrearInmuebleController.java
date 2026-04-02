package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
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
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class CrearInmuebleController {

    private final InmuebleService inmuebleService;

    public CrearInmuebleController() {
        this.inmuebleService = new InmuebleService();
    }

    @GetMapping("/inmuebles/crear")
    public String showCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            Model model) {
        if (asesor == null) {
            return "redirect:/login";
        }
        cargarCombos(model);
        model.addAttribute("titulo", "Crear Inmueble");
        return "crear-inmueble";
    }

    @PostMapping("/inmuebles/crear")
    public String processCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam String codigo,
            @RequestParam String nombre,
            @RequestParam String direccion,
            @RequestParam String ciudad,
            @RequestParam String barrio,
            @RequestParam TipoInmueble tipoInmueble,
            @RequestParam Finalidad finalidad,
            @RequestParam String precio,
            @RequestParam double area,
            @RequestParam int numeroHabitaciones,
            @RequestParam int numeroBanos,
            @RequestParam EstadoInmueble estadoInmueble,
            @RequestParam Disponibilidad disponibilidad,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        try {
            float precioParseado = parsearPrecio(precio);

            inmuebleService.registrarInmueble(
                    codigo,
                    nombre,
                    direccion,
                    ciudad,
                    barrio,
                    asesor.getIdentificacion(),
                    tipoInmueble,
                    finalidad,
                    precioParseado,
                    area,
                    numeroHabitaciones,
                    numeroBanos,
                    estadoInmueble,
                    disponibilidad
            );
            model.addAttribute("mensaje", "Inmueble registrado exitosamente.");
            cargarCombos(model);
            model.addAttribute("titulo", "Crear Inmueble");
            return "crear-inmueble";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("codigo", codigo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("direccion", direccion);
            model.addAttribute("ciudad", ciudad);
            model.addAttribute("barrio", barrio);
            model.addAttribute("precio", precio);
            model.addAttribute("area", area);
            model.addAttribute("numeroHabitaciones", numeroHabitaciones);
            model.addAttribute("numeroBanos", numeroBanos);
            cargarCombos(model);
            model.addAttribute("titulo", "Crear Inmueble");
            return "crear-inmueble";
        }
    }

    private float parsearPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es requerido");
        }

        String valor = precioTexto.trim().replace(" ", "");

        if (valor.contains(",")) {
            valor = valor.replace(".", "");
            valor = valor.replace(",", ".");
        } else {
            int ultimoPunto = valor.lastIndexOf('.');
            if (ultimoPunto > 0) {
                String parteEntera = valor.substring(0, ultimoPunto).replace(".", "");
                String parteDecimal = valor.substring(ultimoPunto + 1);
                valor = parteEntera + "." + parteDecimal;
            } else {
                valor = valor.replace(".", "");
            }
        }

        try {
            return Float.parseFloat(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de precio inválido");
        }
    }

    private void cargarCombos(Model model) {
        model.addAttribute("tiposInmueble", TipoInmueble.values());
        model.addAttribute("finalidades", Finalidad.values());
        model.addAttribute("estadosInmueble", EstadoInmueble.values());
        model.addAttribute("disponibilidades", Disponibilidad.values());
    }

    @GetMapping("/inmuebles/cancelar")
    public String cancelarCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor) {
        if (asesor == null) {
            return "redirect:/login";
        }
        return "redirect:/home";
    }
}