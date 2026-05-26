package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Repositorio.VisitaRepositorio;
import com.uniquindio.Service.InmuebleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Stream;

import java.text.NumberFormat;
import java.util.*;
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
        
        // Obtener presupuesto del cliente y formatearlo
        double presupuestoCliente = cliente.getPresupuesto();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
        String presupuestoFormato = currencyFormat.format(presupuestoCliente);
        model.addAttribute("infoDerecha", "Presupuesto: " + presupuestoFormato + "/mes");

        InmuebleService inmuebleService = new InmuebleService();
        List<Inmueble> todosInmuebles = inmuebleService.obtenerTodosInmuebles();
        
        // Aplicar filtros
        List<Inmueble> inmueblesFiltered = aplicarFiltros(todosInmuebles, zona, tipo, precioMax, habMin);
        
        // Limitar a 5 inmuebles para la página principal
        List<Inmueble> inmuebles = inmueblesFiltered.size() > 5 ? inmueblesFiltered.subList(0, 5) : inmueblesFiltered;
        
        // Obtener inmuebles recomendados basados en presupuesto del cliente
        // y sus preferencias (zona de interés, tipo de inmueble, cantidad de habitaciones)
        List<Inmueble> recomendados = todosInmuebles.stream()
            .filter(i -> i.getPrecio() <= presupuestoCliente)
            .filter(i -> i.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE)
            // Filtrar por zonas de interés del cliente (comparar por ciudad o barrio)
            .filter(i -> cliente.getZonasDeInteres() == null || cliente.getZonasDeInteres().isEmpty() ||
                cliente.getZonasDeInteres().stream().anyMatch(z -> coincideConZonaInteres(z, i)))
            // Filtrar por tipo de inmueble deseado
            .filter(i -> cliente.getTipoInmuebleDeseado() == null ||
                i.getTipoInmueble() == cliente.getTipoInmuebleDeseado())
            // Filtrar por cantidad mínima de habitaciones
            .filter(i -> cliente.getCantidadMinimaHabitaciones() <= 0 ||
                i.getNumeroHabitaciones() >= cliente.getCantidadMinimaHabitaciones())
            // Priorizar coincidencias más fuertes para que aparezcan primero en el bloque recomendado
            .sorted(Comparator
                .comparingInt((Inmueble i) -> calcularPuntajeRecomendado(i, cliente))
                .reversed()
                .thenComparing(Inmueble::getPrecio))
            .collect(Collectors.toList());
        
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

        int favoritosCount = cliente.getFavoritos() != null ? cliente.getFavoritos().size() : 0;

        // Contar visitas agendadas del cliente
        VisitaRepositorio visitaRepositorio = new VisitaRepositorio();
        long visitasAgendadas = visitaRepositorio.obtenerVisitas().values().stream()
                .filter(v -> v != null && v.getCliente() != null && v.getCliente().getIdentificacion().equals(cliente.getIdentificacion()))
                .count();

        // Métricas (por defecto vacías) y etiquetas
        model.addAttribute("metric1", inmuebles.size());
        model.addAttribute("metric1Label", "Consultados");
        model.addAttribute("metric2", favoritosCount);
        model.addAttribute("metric2Label", "Favoritos");
        model.addAttribute("metric3", (int) visitasAgendadas);
        model.addAttribute("metric3Label", "Visitas agendadas");
        model.addAttribute("metric4", "--");
        model.addAttribute("metric4Label", "En negociación");

        model.addAttribute("favoritosCount", favoritosCount);

        // Lista principal de inmuebles reales
        model.addAttribute("itemsPrincipales", inmuebles);
        model.addAttribute("recomendados", recomendados);
        model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(Locale.of("es", "CO")));
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

        String codigoLimpio = codigo != null ? codigo.trim() : "";
        InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
        Inmueble inmueble = inmuebleRepositorio.obtenerInmueble(codigoLimpio);

        if (inmueble == null && !codigoLimpio.isEmpty()) {
            inmueble = inmuebleRepositorio.obtenerInmuebles().values().stream()
                    .filter(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equals(codigoLimpio))
                    .findFirst()
                    .orElse(null);
        }

        if (inmueble == null) {
            return "redirect:/home-cliente";
        }

        cliente.registrarConsulta(inmueble);
        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        clienteRepositorio.crearCliente(cliente);

        final Inmueble inmuebleFinal = inmueble;
        boolean esFavorito = cliente.getFavoritos() != null
            && cliente.getFavoritos().stream()
            .anyMatch(i -> i != null
                && i.getCodigo() != null
                && inmuebleFinal.getCodigo() != null
                && i.getCodigo().trim().equals(inmuebleFinal.getCodigo().trim()));

        model.addAttribute("titulo", "Detalle del Inmueble");
        model.addAttribute("cliente", cliente);
        model.addAttribute("inmueble", inmueble);
        model.addAttribute("esFavorito", esFavorito);
        model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(Locale.of("es", "CO")));
        
        return "detalle-inmueble";
    }

    

    @GetMapping("/favoritos")
    public String showFavoritos(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "ordenar", required = false) String ordenar,
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

            InmuebleService inmuebleService = new InmuebleService();
            List<Inmueble> inmueblesExistentes = inmuebleService.obtenerTodosInmuebles();
            Set<String> codigosExistentes = inmueblesExistentes.stream()
                .map(Inmueble::getCodigo)
                .filter(Objects::nonNull)
                .map(codigo -> codigo.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

            List<Inmueble> favoritos = cliente.getFavoritos() != null ? new ArrayList<>(cliente.getFavoritos()) : new ArrayList<>();
            favoritos = favoritos.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getCodigo() != null)
                .filter(i -> codigosExistentes.contains(i.getCodigo().trim().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        List<Inmueble> favoritosFiltrados = aplicarFiltrosFavoritos(favoritos, q, tipo, estado, filtro, ordenar);

        long disponibles = favoritosFiltrados.stream().filter(i -> i != null && i.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE).count();
        long enNegociacion = favoritosFiltrados.stream().filter(i -> i != null && i.getDisponibilidad() == Inmueble.Disponibilidad.NO_DISPONIBLE).count();
        long reservados = favoritosFiltrados.stream().filter(i -> i != null && i.getDisponibilidad() == Inmueble.Disponibilidad.RESERVADO).count();

        model.addAttribute("titulo", "Mis favoritos");
        model.addAttribute("cliente", cliente);
        model.addAttribute("nombreCliente", cliente.getNombre());
        model.addAttribute("inicialesCliente", iniciales);
        model.addAttribute("rolCliente", "Cliente activa");
        model.addAttribute("favoritos", favoritosFiltrados);
        model.addAttribute("favoritosTotal", favoritosFiltrados.size());
        model.addAttribute("favoritosDisponibles", disponibles);
        model.addAttribute("favoritosNegociacion", enNegociacion);
        model.addAttribute("favoritosReservados", reservados);
        model.addAttribute("tituloEncabezado", "Mis favoritos");
        model.addAttribute("resumenFavoritos", favoritosFiltrados.size() + " inmuebles guardados · " + disponibles + " disponibles");
        model.addAttribute("resultadoFavoritos", favoritosFiltrados.size() + " inmuebles favoritos");
        model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(Locale.of("es", "CO")));

        return "favoritos-cliente";
    }

    private List<Inmueble> aplicarFiltrosFavoritos(List<Inmueble> favoritos,
                                                  String q,
                                                  String tipo,
                                                  String estado,
                                                  String filtro,
                                                  String ordenar) {
        Stream<Inmueble> stream = favoritos.stream().filter(Objects::nonNull);

        if (q != null && !q.trim().isEmpty()) {
            String consulta = q.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(i -> contieneTexto(i.getNombre(), consulta)
                    || contieneTexto(i.getDireccion(), consulta)
                    || contieneTexto(i.getCiudad(), consulta)
                    || contieneTexto(i.getBarrio(), consulta)
                    || contieneTexto(i.getCodigo(), consulta));
        }

        if (tipo != null && !tipo.trim().isEmpty()) {
            stream = stream.filter(i -> i.getTipoInmueble() != null && i.getTipoInmueble().name().equalsIgnoreCase(tipo.trim()));
        }

        if (estado != null && !estado.trim().isEmpty()) {
            stream = stream.filter(i -> coincideEstado(i, estado));
        }

        if (filtro != null && !filtro.trim().isEmpty()) {
            switch (filtro.trim().toLowerCase(Locale.ROOT)) {
                case "disponibles" -> stream = stream.filter(i -> i.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE);
                case "con-visita" -> { /* pendiente de integración con visitas */ }
                case "negociacion" -> stream = stream.filter(i -> i.getDisponibilidad() == Inmueble.Disponibilidad.NO_DISPONIBLE);
                default -> { }
            }
        }

        Comparator<Inmueble> comparator = switch (ordenar != null ? ordenar.trim().toLowerCase(Locale.ROOT) : "") {
    case "precio-asc" -> Comparator.comparingDouble(Inmueble::getPrecio);
    case "precio-desc" -> Comparator.comparingDouble(Inmueble::getPrecio).reversed();
    case "area" -> Comparator.comparingDouble(Inmueble::getArea).reversed();
    default -> Comparator.comparing(Inmueble::getCodigo, Comparator.nullsLast(String::compareToIgnoreCase)).reversed();
};

        return stream.sorted(comparator).collect(Collectors.toList());
    }

    private boolean contieneTexto(String valor, String consulta) {
        return valor != null && valor.trim().toLowerCase(Locale.ROOT).contains(consulta);
    }

    private boolean coincideEstado(Inmueble inmueble, String estado) {
        if (inmueble == null || estado == null) {
            return false;
        }

        String estadoNormalizado = estado.trim().toUpperCase(Locale.ROOT);
        return switch (estadoNormalizado) {
            case "DISPONIBLE" -> inmueble.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE;
            case "RESERVADO" -> inmueble.getDisponibilidad() == Inmueble.Disponibilidad.RESERVADO;
            case "NEGOCIANDO" -> inmueble.getDisponibilidad() == Inmueble.Disponibilidad.NO_DISPONIBLE;
            default -> true;
        };
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

    private int calcularPuntajeRecomendado(Inmueble inmueble, Cliente cliente) {
        int puntaje = 0;

        if (inmueble == null || cliente == null) {
            return puntaje;
        }

        if (cliente.getZonasDeInteres() != null && !cliente.getZonasDeInteres().isEmpty()
                && inmueble.getCiudad() != null
                && cliente.getZonasDeInteres().stream().anyMatch(z -> z != null && z.equalsIgnoreCase(inmueble.getCiudad()))) {
            puntaje += 4;
        }

        if (cliente.getTipoInmuebleDeseado() != null && inmueble.getTipoInmueble() == cliente.getTipoInmuebleDeseado()) {
            puntaje += 3;
        }

        if (cliente.getCantidadMinimaHabitaciones() > 0
                && inmueble.getNumeroHabitaciones() >= cliente.getCantidadMinimaHabitaciones()) {
            puntaje += 2;
        }

        return puntaje;
    }

    private boolean coincideConZonaInteres(String zonaInteres, Inmueble inmueble) {
        if (zonaInteres == null || inmueble == null) {
            return false;
        }

        String zonaNormalizada = zonaInteres.trim().toLowerCase(Locale.ROOT);
        String ciudadNormalizada = inmueble.getCiudad() != null ? inmueble.getCiudad().trim().toLowerCase(Locale.ROOT) : "";
        String barrioNormalizado = inmueble.getBarrio() != null ? inmueble.getBarrio().trim().toLowerCase(Locale.ROOT) : "";

        return !zonaNormalizada.isEmpty()
                && (zonaNormalizada.equals(ciudadNormalizada)
                || zonaNormalizada.equals(barrioNormalizado));
    }
    
    @PostMapping("/favoritos/agregar")
    @ResponseBody
    public ResponseEntity<?> agregarFavorito(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam String codigoInmueble) {
        
        if (cliente == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        
        // Trim del código para evitar espacios
        String codigoLimpio = codigoInmueble.trim();
        System.out.println("DEBUG: Buscando inmueble con código: [" + codigoLimpio + "]");
        
        // Obtener el inmueble directamente desde el repositorio
        InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
        Inmueble inmueble = inmuebleRepositorio.obtenerInmueble(codigoLimpio);
        if (inmueble == null && !codigoLimpio.isEmpty()) {
            inmueble = inmuebleRepositorio.obtenerInmuebles().values().stream()
                .filter(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equals(codigoLimpio))
                .findFirst()
                .orElse(null);
        }
        
        if (inmueble == null) {
            System.out.println("DEBUG: Inmueble no encontrado. Códigos disponibles:");
            inmuebleRepositorio.obtenerInmuebles().values()
                    .forEach(i -> System.out.println("  - [" + (i.getCodigo() != null ? i.getCodigo().trim() : "null") + "]"));
            return ResponseEntity.badRequest().body(Map.of("error", "Inmueble no encontrado", "codigoBuscado", codigoLimpio));
        }
        
        System.out.println("DEBUG: Inmueble encontrado: " + inmueble.getNombre());
        
        // Inicializar lista de favoritos si es nula
        if (cliente.getFavoritos() == null) {
            cliente.setFavoritos(new ArrayList<>());
        }
        
        // Verificar si ya está en favoritos
        boolean yaEnFavoritos = cliente.getFavoritos()
                .stream()
                .anyMatch(i -> i.getCodigo() != null && i.getCodigo().trim().equals(codigoLimpio));
        
        if (yaEnFavoritos) {
            cliente.getFavoritos().removeIf(i -> i.getCodigo() != null && i.getCodigo().trim().equals(codigoLimpio));
        } else {
            cliente.getFavoritos().add(inmueble);
            if (cliente.getIdentificacion() != null) {
                com.uniquindio.Service.GrafoService.getInstancia()
                        .registrarFavorito(cliente.getIdentificacion(), codigoLimpio);
            }
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        clienteRepositorio.crearCliente(cliente);
        
        System.out.println("DEBUG: Favorito " + (yaEnFavoritos ? "removido" : "agregado") + " exitosamente");
        
        return ResponseEntity.ok(Map.of(
            "exito", true,
            "enFavoritos", !yaEnFavoritos,
            "mensaje", !yaEnFavoritos ? "Agregado a favoritos" : "Removido de favoritos"
        ));
    }

    @PostMapping("/favoritos/quitar")
    public String quitarFavorito(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            @RequestParam String codigoInmueble) {
        if (cliente == null) {
            return "redirect:/login";
        }

        String codigoLimpio = codigoInmueble != null ? codigoInmueble.trim() : "";
        if (cliente.getFavoritos() != null && !codigoLimpio.isEmpty()) {
            cliente.getFavoritos().removeIf(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equals(codigoLimpio));
            ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
            clienteRepositorio.crearCliente(cliente);
        }

        return "redirect:/favoritos";
    }
}
