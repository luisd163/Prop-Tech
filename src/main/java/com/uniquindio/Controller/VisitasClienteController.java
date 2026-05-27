package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.VisitaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class VisitasClienteController {

    @GetMapping("/visitas-cliente")
    public String showVisitasCliente(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam(name = "q", required = false) String q,
            Model model
    ) {
        if (cliente == null) {
            return "redirect:/login";
        }

        VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
        List<Visita> visitas = new ArrayList<>(visitaRepositorio.obtenerVisitas().values());

        String consulta = q == null ? "" : q.trim().toLowerCase();

        Map<String, Visita> visitasProgramadasPorInmueble = visitas.stream()
                .filter(Objects::nonNull)
                .filter(this::esVisitaProgramada)
                .filter(visita -> visita.getCliente() != null
                        && visita.getCliente().getIdentificacion() != null
                        && visita.getCliente().getIdentificacion().equals(cliente.getIdentificacion()))
                .filter(visita -> visita.getInmueble() != null
                        && visita.getInmueble().getCodigo() != null)
                .collect(Collectors.toMap(
                        visita -> visita.getInmueble().getCodigo().trim(),
                        visita -> visita,
                        this::tomarVisitaMasTemprana,
                        LinkedHashMap::new
                ));

        List<Visita> visitasProgramadas = new ArrayList<>(visitasProgramadasPorInmueble.values());
        visitasProgramadas = visitasProgramadas.stream()
                .filter(visita -> {
                    if (consulta.isEmpty()) {
                        return true;
                    }
                    Inmueble inmueble = visita.getInmueble();
                    String nombre = inmueble.getNombre() != null ? inmueble.getNombre().toLowerCase() : "";
                    String direccion = inmueble.getDireccion() != null ? inmueble.getDireccion().toLowerCase() : "";
                    String ciudad = inmueble.getCiudad() != null ? inmueble.getCiudad().toLowerCase() : "";
                    String barrio = inmueble.getBarrio() != null ? inmueble.getBarrio().toLowerCase() : "";
                    return nombre.contains(consulta)
                            || direccion.contains(consulta)
                            || ciudad.contains(consulta)
                            || barrio.contains(consulta);
                })
                .sorted(Comparator
                        .comparing(Visita::getFecha, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Visita::getHora, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        String nombreCliente = cliente.getNombre() != null ? cliente.getNombre() : "Cliente";
        model.addAttribute("titulo", "Mis visitas");
        model.addAttribute("cliente", cliente);
        model.addAttribute("nombreCliente", nombreCliente);
        model.addAttribute("rolCliente", "Cliente");
        model.addAttribute("inicialesCliente", obtenerIniciales(nombreCliente));
        model.addAttribute("visitasProgramadas", visitasProgramadas);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("totalVisitasProgramadas", visitasProgramadas.size());

        return "visitas-cliente";
    }

    private boolean esVisitaProgramada(Visita visita) {
        if (visita == null || visita.getEstado() == null) {
            return false;
        }

        return visita.getEstado() == Visita.EstadoVisita.PENDIENTE
                || visita.getEstado() == Visita.EstadoVisita.CONFIRMADA
                || visita.getEstado() == Visita.EstadoVisita.REPROGRAMADA;
    }

    private Visita tomarVisitaMasTemprana(Visita actual, Visita nueva) {
        if (actual == null) {
            return nueva;
        }
        if (nueva == null) {
            return actual;
        }

        LocalDate fechaActual = actual.getFecha();
        LocalDate fechaNueva = nueva.getFecha();
        if (fechaActual == null && fechaNueva != null) {
            return nueva;
        }
        if (fechaActual != null && fechaNueva == null) {
            return actual;
        }
        if (fechaActual != null && fechaNueva != null) {
            int comparacionFecha = fechaNueva.compareTo(fechaActual);
            if (comparacionFecha < 0) {
                return nueva;
            }
            if (comparacionFecha > 0) {
                return actual;
            }

            LocalTime horaActual = actual.getHora();
            LocalTime horaNueva = nueva.getHora();
            if (horaActual == null && horaNueva != null) {
                return nueva;
            }
            if (horaActual != null && horaNueva == null) {
                return actual;
            }
            if (horaActual != null && horaNueva != null && horaNueva.isBefore(horaActual)) {
                return nueva;
            }
        }

        return actual;
    }

    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "CL";
        }

        String[] partes = nombre.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();

        for (String parte : partes) {
            if (!parte.isBlank()) {
                iniciales.append(Character.toUpperCase(parte.charAt(0)));
            }
            if (iniciales.length() == 2) {
                break;
            }
        }

        return iniciales.length() == 0 ? "CL" : iniciales.toString();
    }
}
