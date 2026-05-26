package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Service.AsesorHomeService;
import com.uniquindio.Service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

@Controller
public class OperacionesController {

    @GetMapping({"/operaciones","/operaciones/asesor"})
        public String showOperaciones(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false) String q,
            @org.springframework.web.bind.annotation.RequestParam(name = "estado", required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(name = "periodo", required = false) String periodo,
            Model model
        ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
        AsesorHomeService asesorHomeService = new AsesorHomeService();

        // inmuebles reales asignados al asesor (instancias canónicas)
        List<Inmueble> inmueblesAsesor = asesorHomeService.obtenerInmueblesAsesor(asesor);
        java.util.Set<String> codigosInmueblesAsesor = new java.util.HashSet<>();
        for (Inmueble inm : inmueblesAsesor) {
            if (inm != null && inm.getCodigo() != null) codigosInmueblesAsesor.add(inm.getCodigo().trim());
        }

        List<Visita> visitas = new ArrayList<>();

        for (Cliente cliente : clienteRepositorio.obtenerClientes().values()) {
            if (cliente == null) continue;
            Inmueble asignado = cliente.getInmuebleAsignado();
            if (asignado == null || asignado.getCodigo() == null) continue;
            String codigo = asignado.getCodigo().trim();
            if (!codigosInmueblesAsesor.contains(codigo)) continue;

            // obtener instancia canónica del repositorio para mostrar datos completos
            Inmueble canon = inmuebleRepositorio.obtenerInmueble(codigo);
            if (canon == null) {
                canon = inmuebleRepositorio.obtenerInmuebles().values().stream()
                        .filter(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equalsIgnoreCase(codigo))
                        .findFirst().orElse(asignado);
            }

            // actualizar referencia local del cliente (no persiste aquí)
            cliente.setInmuebleAsignado(canon);

            Visita v = new Visita();
            v.setId(cliente.getIdentificacion() + "-" + (canon.getCodigo() == null ? "-" : canon.getCodigo()));
            v.setCliente(cliente);
            v.setInmueble(canon);
            v.setAsesorId(asesor.getIdentificacion());
            v.setEstado(Visita.EstadoVisita.PENDIENTE);
            visitas.add(v);
        }

        model.addAttribute("visitas", visitas);
        // aplicar filtros y búsqueda
        VisitaService visitaService = new VisitaService();

        if (periodo == null || periodo.isBlank()) periodo = "semana";
        if (estado == null || estado.isBlank()) estado = "TODAS";

        if (periodo.equalsIgnoreCase("semana")) {
            visitas = visitaService.filtrarEstaSemana(visitas);
        }

        if (!"TODAS".equalsIgnoreCase(estado)) {
            try {
                Visita.EstadoVisita estadoEnum = Visita.EstadoVisita.valueOf(estado);
                visitas = visitaService.filtrarPorEstado(visitas, estadoEnum);
            } catch (IllegalArgumentException ignored) {
            }
        }

        visitas = visitaService.buscarPorClienteOInmueble(visitas, q);

        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("estadoFiltro", estado == null ? "TODAS" : estado);
        model.addAttribute("periodoFiltro", periodo == null ? "semana" : periodo);

        model.addAttribute("kpiTotalMes", visitas.size());
        model.addAttribute("kpiPendientes", visitas.size());
        model.addAttribute("kpiConfirmadas", 0);
        model.addAttribute("kpiRealizadas", 0);
        model.addAttribute("kpiCanceladas", 0);

        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", "Asesor inmobiliario");
        model.addAttribute("inicialesAsesor", obtenerIniciales(asesor.getNombre()));

        return "operaciones-asesor";
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
