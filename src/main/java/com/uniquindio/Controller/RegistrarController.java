package com.uniquindio.Controller;

import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Service.RegistrarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrarController {

    private final RegistrarService registrarService;

    public RegistrarController() {
        this.registrarService = new RegistrarService();
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam String identificacion,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String telefono,
            @RequestParam String tipoUsuario,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) Double comisionPorcentaje,
            @RequestParam(required = false) String tipoCliente,
            @RequestParam(required = false) Double presupuesto,
            @RequestParam(required = false) String zonasDeInteres,
            @RequestParam(required = false) String tipoInmuebleDeseado,
            @RequestParam(required = false) Integer cantidadMinimaHabitaciones,
            @RequestParam(required = false) String estadoBusqueda,
            Model model) {

        if (identificacion == null || identificacion.isBlank()
                || nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()
                || email == null || email.isBlank() || telefono == null || telefono.isBlank()
                || tipoUsuario == null || tipoUsuario.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Todos los campos son requeridos");
            return "registro";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "registro";
        }

        try {
            String nombreCompleto = (nombre + " " + apellido).trim();
            String tipoNormalizado = tipoUsuario.trim().toLowerCase();

            if (tipoNormalizado.equals("asesor")) {
                if (especialidad == null || especialidad.isBlank() || comisionPorcentaje == null) {
                    model.addAttribute("error", "Completa especialidad y comisión para registrar un asesor");
                    return "registro";
                }
                registrarService.registrarAsesor(identificacion, nombreCompleto, email, telefono, password,
                        especialidad, comisionPorcentaje);
            } else if (tipoNormalizado.equals("cliente")) {
                Cliente.TipoCliente tipoClienteEnum = Cliente.TipoCliente.PERSONA_NATURAL;
                if (tipoCliente != null && !tipoCliente.isBlank()) {
                    tipoClienteEnum = Cliente.TipoCliente.valueOf(tipoCliente);
                }

                double presupuestoValor = presupuesto != null ? presupuesto : 0;
                int cantidadHabitaciones = cantidadMinimaHabitaciones != null ? cantidadMinimaHabitaciones : 1;
                Inmueble.TipoInmueble tipoDeseado = Inmueble.TipoInmueble.CASA;
                if (tipoInmuebleDeseado != null && !tipoInmuebleDeseado.isBlank()) {
                    tipoDeseado = Inmueble.TipoInmueble.valueOf(tipoInmuebleDeseado);
                }

                Cliente.EstadoBusqueda estadoBusquedaEnum = Cliente.EstadoBusqueda.ACTIVA;
                if (estadoBusqueda != null && !estadoBusqueda.isBlank()) {
                    estadoBusquedaEnum = Cliente.EstadoBusqueda.valueOf(estadoBusqueda);
                }

                registrarService.registrarClienteCompleto(
                        identificacion,
                        nombreCompleto,
                        email,
                        telefono,
                        password,
                        tipoClienteEnum,
                        presupuestoValor,
                        zonasDeInteres,
                        tipoDeseado,
                        cantidadHabitaciones,
                        estadoBusquedaEnum);
            } else {
                model.addAttribute("error", "Solo se permite registrar Cliente o Asesor");
                return "registro";
            }

            model.addAttribute("message", "Usuario registrado exitosamente");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}
