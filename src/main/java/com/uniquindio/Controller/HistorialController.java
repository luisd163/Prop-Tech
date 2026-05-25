package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.HistorialItem;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Repositorio.ClienteRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Controller
public class HistorialController {

    private static final int TAMANO_PAGINA = 8;

    @GetMapping({"/historial", "/historial-cliente"})
    public String showHistorial(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "pagina", required = false, defaultValue = "0") int pagina,
            Model model) {

        if (cliente == null) {
            return "redirect:/login";
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        Cliente clienteActual = clienteRepositorio.obtenerClientes().get(cliente.getIdentificacion());
        if (clienteActual == null) {
            clienteActual = cliente;
        }

        List<HistorialItem> historialCompleto = construirHistorial(clienteActual);
        String busqueda = q != null ? q.trim().toLowerCase(Locale.ROOT) : "";
        String accionFiltro = accion != null ? accion.trim().toUpperCase(Locale.ROOT) : "";

        List<HistorialItem> historialFiltrado = historialCompleto.stream()
                .filter(item -> coincideBusqueda(item, busqueda))
                .filter(item -> accionFiltro.isEmpty() || accionFiltro.equals(item.getAccion()))
                .toList();

        int totalInteracciones = historialFiltrado.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(totalInteracciones / (double) TAMANO_PAGINA));
        int paginaActual = Math.max(0, Math.min(pagina, totalPaginas - 1));
        int desde = Math.min(paginaActual * TAMANO_PAGINA, totalInteracciones);
        int hasta = Math.min(desde + TAMANO_PAGINA, totalInteracciones);

        List<HistorialItem> historial = totalInteracciones == 0
                ? Collections.emptyList()
                : new ArrayList<>(historialFiltrado.subList(desde, hasta));

        List<HistorialItem> actividadReciente = historialFiltrado.stream()
                .limit(5)
                .toList();

        long totalConsultados = historialFiltrado.stream().filter(i -> "CONSULTADO".equals(i.getAccion())).count();
        long totalFavoritos = historialFiltrado.stream().filter(i -> "FAVORITO".equals(i.getAccion())).count();
        long totalVisitados = historialFiltrado.stream().filter(i -> "VISITADO".equals(i.getAccion())).count();
        long totalNegociados = historialFiltrado.stream().filter(i -> "NEGOCIANDO".equals(i.getAccion())).count();

        model.addAttribute("cliente", clienteActual);
        model.addAttribute("nombreCliente", clienteActual.getNombre());
        model.addAttribute("inicialesCliente", construirIniciales(clienteActual.getNombre()));
        model.addAttribute("historial", historial);
        model.addAttribute("actividadReciente", actividadReciente);
        model.addAttribute("totalInteracciones", totalInteracciones);
        model.addAttribute("totalConsultados", totalConsultados);
        model.addAttribute("totalFavoritos", totalFavoritos);
        model.addAttribute("totalVisitados", totalVisitados);
        model.addAttribute("totalNegociados", totalNegociados);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("totalPaginas", totalPaginas);

        return "historial-cliente";
    }

    private List<HistorialItem> construirHistorial(Cliente cliente) {
        List<HistorialItem> historial = new ArrayList<>();

        if (cliente.getHistorialConsultas() != null) {
            for (Inmueble inmueble : cliente.getHistorialConsultas()) {
                if (inmueble != null) {
                    historial.add(crearItem(inmueble, "CONSULTADO", "Consultado recientemente"));
                }
            }
        }

        if (cliente.getFavoritos() != null) {
            for (Inmueble inmueble : cliente.getFavoritos()) {
                if (inmueble != null) {
                    historial.add(crearItem(inmueble, "FAVORITO", "Agregado a favoritos"));
                }
            }
        }

        if (cliente.getIntenciones() != null) {
            for (Inmueble inmueble : cliente.getIntenciones()) {
                if (inmueble != null) {
                    historial.add(crearItem(inmueble, "NEGOCIANDO", "Marcado como intención"));
                }
            }
        }

        return historial;
    }

    private HistorialItem crearItem(Inmueble inmueble, String accion, String fechaTexto) {
        return HistorialItem.builder()
                .inmueble(inmueble)
                .accion(accion)
                .fechaTexto(fechaTexto)
                .build();
    }

    private boolean coincideBusqueda(HistorialItem item, String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) {
            return true;
        }

        Inmueble inmueble = item.getInmueble();
        if (inmueble == null) {
            return false;
        }

        return contiene(inmueble.getNombre(), busqueda)
                || contiene(inmueble.getCodigo(), busqueda)
                || contiene(inmueble.getBarrio(), busqueda)
                || contiene(inmueble.getCiudad(), busqueda)
                || contiene(inmueble.getTipoInmueble() != null ? inmueble.getTipoInmueble().name() : null, busqueda);
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    private String construirIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "CL";
        }

        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase(Locale.ROOT);
        }

        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }
}
