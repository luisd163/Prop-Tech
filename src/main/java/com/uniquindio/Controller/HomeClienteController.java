package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.text.NumberFormat;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class HomeClienteController {

    @GetMapping("/home-cliente")
    public String showHomeCliente(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam(name = "zona", required = false) String zona,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "precioMax", required = false) String precioMax,
            @RequestParam(name = "habMin", required = false) String habMin,
            Model model) {
        if (cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("titulo", "Panel Cliente");
        model.addAttribute("cliente", cliente);
        model.addAttribute("nombreCliente", cliente.getNombre());
        model.addAttribute("saludo", "Bienvenido(a), " + cliente.getNombre());

        // Placeholders / valores por defecto para la plantilla
        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        String iniciales = "CL";
        if (!nombre.isEmpty()) {
            String[] parts = nombre.split("\\s+");
            if (parts.length == 1) {
                iniciales = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            } else {
                String p1 = parts[0].substring(0, 1);
                String p2 = parts[1].substring(0, 1);
                iniciales = (p1 + p2).toUpperCase();
            }
        }

        model.addAttribute("inicialesCliente", iniciales);
        model.addAttribute("rolCliente", "Cliente");
        model.addAttribute("infoDerecha", "Presupuesto: $0/mes");

        InmuebleService inmuebleService = new InmuebleService();
        List<Inmueble> todosInmuebles = inmuebleService.obtenerTodosInmuebles();
        
        // Aplicar filtros
        List<Inmueble> inmueblesFiltered = aplicarFiltros(todosInmuebles, zona, tipo, precioMax, habMin);
        
        // Limitar a 5 inmuebles para la página principal
        List<Inmueble> inmuebles = inmueblesFiltered.size() > 5 ? inmueblesFiltered.subList(0, 5) : inmueblesFiltered;
        
        // Generar opciones dinámicas para selects
        Set<String> zonas = todosInmuebles.stream()
                .map(Inmueble::getBarrio)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<String> tipos = todosInmuebles.stream()
                .map(i -> i.getTipoInmueble() != null ? i.getTipoInmueble().toString() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        model.addAttribute("subtitulo", "Tienes " + inmuebles.size() + " inmuebles disponibles para explorar");
        model.addAttribute("zonasDisponibles", zonas);
        model.addAttribute("tiposDisponibles", tipos);
        model.addAttribute("filtroZona", zona);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("filtroPrecioMax", precioMax);
        model.addAttribute("filtroHabMin", habMin);

        // Métricas (por defecto vacías) y etiquetas
        model.addAttribute("metric1", inmuebles.size());
        model.addAttribute("metric1Label", "Consultados");
        model.addAttribute("metric2", "--");
        model.addAttribute("metric2Label", "Favoritos");
        model.addAttribute("metric3", "--");
        model.addAttribute("metric3Label", "Visitas agendadas");
        model.addAttribute("metric4", "--");
        model.addAttribute("metric4Label", "En negociación");

        // Lista principal de inmuebles reales
        model.addAttribute("itemsPrincipales", inmuebles);
        model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(new Locale("es", "CO")));
        return "home-cliente";
    }
    
    @GetMapping("/detalle-inmueble/{codigo}")
    public String showDetalleInmueble(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @PathVariable String codigo,
            Model model) {
        if (cliente == null) {
            return "redirect:/login";
        }

        InmuebleService inmuebleService = new InmuebleService();
        Inmueble inmueble = inmuebleService.obtenerTodosInmuebles()
                .stream()
                .filter(i -> i.getCodigo() != null && i.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);

        if (inmueble == null) {
            return "redirect:/home-cliente";
        }

        model.addAttribute("titulo", "Detalle del Inmueble");
        model.addAttribute("cliente", cliente);
        model.addAttribute("inmueble", inmueble);
        model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(new Locale("es", "CO")));
        
        return "detalle-inmueble";
    }

    @GetMapping("/inmuebles-cliente")
    public String showInmueblesCliente(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            Model model) {
        if (cliente == null) {
            return "redirect:/login";
        }

        String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
        String iniciales = "CL";
        if (!nombre.isEmpty()) {
            String[] parts = nombre.split("\\s+");
            if (parts.length == 1) {
                iniciales = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            } else {
                String p1 = parts[0].substring(0, 1);
                String p2 = parts[1].substring(0, 1);
                iniciales = (p1 + p2).toUpperCase();
            }
        }

        model.addAttribute("titulo", "Inmuebles disponibles");
        model.addAttribute("nombreCliente", cliente.getNombre());
        model.addAttribute("inicialesCliente", iniciales);
        model.addAttribute("rolCliente", "Cliente activa");

        return "inmuebles-cliente";
    }
    
    private List<Inmueble> aplicarFiltros(List<Inmueble> inmuebles, String zona, String tipo, String precioMax, String habMin) {
        return inmuebles.stream()
                .filter(i -> zona == null || zona.isEmpty() || 
                        (i.getBarrio() != null && i.getBarrio().equalsIgnoreCase(zona)))
                .filter(i -> tipo == null || tipo.isEmpty() || 
                        (i.getTipoInmueble() != null && i.getTipoInmueble().toString().equals(tipo)))
                .filter(i -> precioMax == null || precioMax.isEmpty() || 
                        (i.getPrecio() <= Float.parseFloat(precioMax)))
                .filter(i -> habMin == null || habMin.isEmpty() || 
                        (i.getNumeroHabitaciones() >= Integer.parseInt(habMin)))
                .collect(Collectors.toList());
    }
}
