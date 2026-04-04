package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.AsesorRepositorio;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Service.AsesorHomeService;
import com.uniquindio.Service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class VisitasAsesorController {

    @GetMapping("/clientes/asignar")
    public String showAsignarCliente(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam(name = "codigo", required = false) String codigo,
            @RequestParam(name = "selectedId", required = false) String selectedId,
            @RequestParam(name = "ok", required = false) String ok,
            @RequestParam(name = "error", required = false) String error,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        AsesorRepositorio asesorRepositorio = new AsesorRepositorio();

        List<Cliente> clientes = new ArrayList<>(clienteRepositorio.obtenerClientes().values());
        if (codigo != null && !codigo.isBlank()) {
            String codigoFiltro = codigo.trim().toLowerCase();
            clientes = clientes.stream()
                    .filter(c -> c != null && c.getIdentificacion() != null
                            && c.getIdentificacion().toLowerCase().contains(codigoFiltro))
                    .collect(Collectors.toList());
        }

        Set<String> clientesDelAsesor = obtenerIdsClientesAsesor(asesor);
        Set<String> clientesAsignadosOtros = obtenerIdsClientesAsignadosAOtrosAsesores(asesor, asesorRepositorio);

        Cliente clienteSeleccionado = null;
        if (selectedId != null && !selectedId.isBlank()) {
            for (Cliente cliente : clientes) {
                if (cliente != null && selectedId.equals(cliente.getIdentificacion())) {
                    clienteSeleccionado = cliente;
                    break;
                }
            }
        }

        if (clienteSeleccionado == null && !clientes.isEmpty()) {
            clienteSeleccionado = clientes.get(0);
        }

        model.addAttribute("clientes", clientes);
        model.addAttribute("clientesDelAsesor", clientesDelAsesor);
        model.addAttribute("clientesAsignadosOtros", clientesAsignadosOtros);
        model.addAttribute("clienteSeleccionado", clienteSeleccionado);
        model.addAttribute("codigo", codigo == null ? "" : codigo);
        model.addAttribute("ok", ok);
        model.addAttribute("error", error);

        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", "Asesor inmobiliario");
        model.addAttribute("inicialesAsesor", obtenerIniciales(asesor.getNombre()));

        return "asignar-cliente";
    }

    @PostMapping("/clientes/asignar")
    public String asignarClienteACartera(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam String clienteId,
            @RequestParam(name = "codigo", required = false) String codigo,
            @RequestParam(name = "selectedId", required = false) String selectedId
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        if (clienteId == null || clienteId.isBlank()) {
            return "redirect:/clientes/asignar?error=Debes seleccionar un cliente";
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        AsesorRepositorio asesorRepositorio = new AsesorRepositorio();
        Cliente cliente = clienteRepositorio.obtenerClientes().get(clienteId);

        if (cliente == null) {
            return "redirect:/clientes/asignar?error=Cliente no encontrado";
        }

        Set<String> clientesAsignadosOtros = obtenerIdsClientesAsignadosAOtrosAsesores(asesor, asesorRepositorio);
        if (clientesAsignadosOtros.contains(clienteId)) {
            return "redirect:/clientes/asignar?error=El cliente ya está asignado a otro asesor";
        }

        if (asesor.getClientes() == null) {
            asesor.setClientes(new ArrayList<>());
        }

        boolean existe = asesor.getClientes().stream()
                .anyMatch(c -> c != null && clienteId.equals(c.getIdentificacion()));

        if (!existe) {
            asesor.getClientes().add(cliente);
            asesorRepositorio.crearAsesor(asesor);
        }

        StringBuilder redirect = new StringBuilder("redirect:/clientes/asignar?ok=Cliente asignado");
        if (codigo != null && !codigo.isBlank()) {
            redirect.append("&codigo=").append(codigo);
        }
        if (selectedId != null && !selectedId.isBlank()) {
            redirect.append("&selectedId=").append(selectedId);
        }
        return redirect.toString();
    }

    @GetMapping("/clientes")
    public String showClientes(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam(name = "codigo", required = false) String codigo,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        List<Cliente> clientesAsesor = asesor.getClientes();
        if (clientesAsesor == null) {
            clientesAsesor = new java.util.ArrayList<>();
        }

        if (codigo != null && !codigo.isBlank()) {
            String codigoFiltro = codigo.trim().toLowerCase();
            clientesAsesor = clientesAsesor.stream()
                    .filter(c -> c != null && c.getIdentificacion() != null
                            && c.getIdentificacion().toLowerCase().contains(codigoFiltro))
                    .collect(Collectors.toList());
        }

        model.addAttribute("clientesAsesor", clientesAsesor);
        model.addAttribute("codigo", codigo == null ? "" : codigo);
        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", "Asesor inmobiliario");
        model.addAttribute("inicialesAsesor", obtenerIniciales(asesor.getNombre()));

        return "clientes";
    }

    @GetMapping("/visitas/crear")
    public String showCrearVisita(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        AsesorHomeService asesorHomeService = new AsesorHomeService();
        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();

        List<Inmueble> inmuebles = asesorHomeService.obtenerInmueblesAsesor(asesor);
        List<Cliente> clientes = clienteRepositorio.obtenerClientes().values().stream().collect(Collectors.toList());

        model.addAttribute("asesor", asesor);
        model.addAttribute("inmuebles", inmuebles);
        model.addAttribute("clientes", clientes);

        return "crear-visita";
    }

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

    private Set<String> obtenerIdsClientesAsesor(Asesor asesor) {
        Set<String> ids = new HashSet<>();
        if (asesor == null || asesor.getClientes() == null) {
            return ids;
        }

        for (Cliente cliente : asesor.getClientes()) {
            if (cliente != null && cliente.getIdentificacion() != null) {
                ids.add(cliente.getIdentificacion());
            }
        }
        return ids;
    }

    private Set<String> obtenerIdsClientesAsignadosAOtrosAsesores(Asesor asesorActual, AsesorRepositorio asesorRepositorio) {
        Set<String> ids = new HashSet<>();
        for (Asesor asesor : asesorRepositorio.obtenerAsesores().values()) {
            if (asesor == null || asesor.getIdentificacion() == null) {
                continue;
            }

            if (asesorActual != null && asesor.getIdentificacion().equals(asesorActual.getIdentificacion())) {
                continue;
            }

            if (asesor.getClientes() == null) {
                continue;
            }

            for (Cliente cliente : asesor.getClientes()) {
                if (cliente != null && cliente.getIdentificacion() != null) {
                    ids.add(cliente.getIdentificacion());
                }
            }
        }
        return ids;
    }
}
