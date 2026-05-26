package com.uniquindio.Service;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Controller.ControladorPrincipal;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Repositorio.VisitaRepositorio;
import com.uniquindio.Model.Visita;
import com.uniquindio.Estructuras.Grafo;

import java.util.*;

public class GrafoService {

    // ─── Instancia singleton ─────────────────────────────────────────────────
    private static GrafoService instancia;

    // ─── Grafos del sistema ───────────────────────────────────────────────────
    private final Grafo<String> grafoClienteInmueble;   // quién visitó qué
    private final Grafo<String> grafoInmuebleZona;       // inmueble pertenece a zona
    private final Grafo<String> grafoZonaCliente;        // qué zonas interesan a cada cliente

    // ─── Repositorios para acceso a datos ─────────────────────────────────────
    private final ClienteRepositorio clienteRepositorio;
    private final InmuebleRepositorio inmuebleRepositorio;
    private final VisitaRepositorio visitaRepositorio;

    // ─── Referencia al controlador principal ─────────────────────────────────
    private final ControladorPrincipal controlador;

    private volatile boolean grafoCargado;

    private GrafoService() {
        this.grafoClienteInmueble = new Grafo<>();
        this.grafoInmuebleZona    = new Grafo<>();
        this.grafoZonaCliente     = new Grafo<>();
        this.controlador          = ControladorPrincipal.getInstancia();
        this.clienteRepositorio   = new ClienteRepositorio();
        this.inmuebleRepositorio  = new InmuebleRepositorio();
        this.visitaRepositorio    = new VisitaRepositorio();
    }

    public static GrafoService getInstancia() {
        if (instancia == null) instancia = new GrafoService();
        return instancia;
    }

    /**
     * Reconstruye los grafos desde visitas realizadas, inmuebles y perfiles de clientes.
     * Idempotente: puede invocarse al arranque sin duplicar aristas.
     */
    public synchronized void cargarDesdePersistencia() {
        if (grafoCargado) {
            return;
        }
        // Marcar antes de registrar relaciones para evitar recursión infinita
        // (registrarFavorito/registrarVisita llaman a asegurarGrafoCargado).
        grafoCargado = true;

        for (Visita visita : visitaRepositorio.obtenerVisitas().values()) {
            if (visita == null || visita.getEstado() != Visita.EstadoVisita.REALIZADA) {
                continue;
            }
            if (visita.getCliente() == null || visita.getInmueble() == null) {
                continue;
            }
            String idCliente = visita.getCliente().getIdentificacion();
            String codigoInm = visita.getInmueble().getCodigo();
            if (idCliente == null || idCliente.isBlank() || codigoInm == null || codigoInm.isBlank()) {
                continue;
            }
            registrarVisita(idCliente, codigoInm);
        }

        for (Inmueble inmueble : inmuebleRepositorio.obtenerInmuebles().values()) {
            if (inmueble != null && inmueble.getCodigo() != null && inmueble.getBarrio() != null) {
                registrarRelacionInmuebleZona(inmueble.getCodigo(), inmueble.getBarrio());
            }
        }

        for (Cliente cliente : clienteRepositorio.obtenerClientes().values()) {
            if (cliente == null || cliente.getIdentificacion() == null) {
                continue;
            }
            if (cliente.getZonasDeInteres() != null) {
                for (String zona : cliente.getZonasDeInteres()) {
                    if (zona != null && !zona.isBlank()) {
                        registrarInteresPorZona(cliente.getIdentificacion(), zona.trim());
                    }
                }
            }
            if (cliente.getFavoritos() != null) {
                for (Inmueble fav : cliente.getFavoritos()) {
                    if (fav != null && fav.getCodigo() != null && !fav.getCodigo().isBlank()) {
                        registrarFavorito(cliente.getIdentificacion(), fav.getCodigo().trim());
                    }
                }
            }
        }
    }

    /**
     * Registra que un cliente guardó un inmueble en favoritos.
     */
    public void registrarFavorito(String idCliente, String codigoInmueble) {
        asegurarGrafoCargado();

        String nodoCliente = "CLI-" + idCliente;
        String nodoInmueble = "INM-" + codigoInmueble;

        grafoClienteInmueble.agregarNodo(nodoCliente);
        grafoClienteInmueble.agregarNodo(nodoInmueble);
        if (!grafoClienteInmueble.tieneArista(nodoCliente, nodoInmueble)) {
            grafoClienteInmueble.agregarArista(nodoCliente, nodoInmueble);
        }

        Inmueble inmueble = inmuebleRepositorio.obtenerInmueble(codigoInmueble);
        if (inmueble == null) {
            inmueble = controlador.buscarInmueblePorCodigo(codigoInmueble);
        }
        if (inmueble != null && inmueble.getBarrio() != null) {
            registrarRelacionInmuebleZona(codigoInmueble, inmueble.getBarrio());
        }
    }

    private void asegurarGrafoCargado() {
        if (!grafoCargado) {
            cargarDesdePersistencia();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. REGISTRO DE RELACIONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra que un cliente visitó un inmueble.
     * Se llama desde VisitaService cuando una visita pasa a REALIZADA.
     */
    public void registrarVisita(String idCliente, String codigoInmueble) {
        asegurarGrafoCargado();

        String nodoCliente  = "CLI-" + idCliente;
        String nodoInmueble = "INM-" + codigoInmueble;

        grafoClienteInmueble.agregarNodo(nodoCliente);
        grafoClienteInmueble.agregarNodo(nodoInmueble);
        if (!grafoClienteInmueble.tieneArista(nodoCliente, nodoInmueble)) {
            grafoClienteInmueble.agregarArista(nodoCliente, nodoInmueble);
        }

        Inmueble inmueble = controlador.buscarInmueblePorCodigo(codigoInmueble);
        if (inmueble != null) {
            registrarRelacionInmuebleZona(codigoInmueble, inmueble.getBarrio());
        }
    }

    /**
     * Registra que un inmueble pertenece a una zona.
     */
    public void registrarRelacionInmuebleZona(String codigoInmueble, String zona) {
        String nodoInmueble = "INM-" + codigoInmueble;
        String nodoZona     = "ZON-" + zona;

        grafoInmuebleZona.agregarNodo(nodoInmueble);
        grafoInmuebleZona.agregarNodo(nodoZona);
        grafoInmuebleZona.agregarArista(nodoInmueble, nodoZona);
    }

    /**
     * Registra que una zona es de interés para un cliente.
     */
    public void registrarInteresPorZona(String idCliente, String zona) {
        String nodoCliente = "CLI-" + idCliente;
        String nodoZona    = "ZON-" + zona;

        grafoZonaCliente.agregarNodo(nodoCliente);
        grafoZonaCliente.agregarNodo(nodoZona);
        grafoZonaCliente.agregarArista(nodoCliente, nodoZona);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. LOCACIONES CERCANAS (para la vista del cliente)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve los datos del grafo de locaciones cercanas para un cliente.
     * El frontend recibe nodos + aristas listos para renderizar con Canvas.
     */
    /**
     * Grafo de relaciones cliente ↔ inmuebles visitados (visitas REALIZADAS).
     * Incluye inmuebles recomendados por comportamiento como nodos secundarios.
     */
    public GrafoDTO obtenerGrafoRelacionesCliente(String idCliente) {
        asegurarGrafoCargado();

        GrafoDTO dto = new GrafoDTO();
        Map<String, Cliente> clientesMap = clienteRepositorio.obtenerClientes();
        Cliente cliente = clientesMap.get(idCliente);

        if (cliente == null) {
            return dto;
        }

        String nodoYoId = "yo-" + idCliente;
        NodoDTO nodoYo = new NodoDTO(nodoYoId, cliente.getNombre() != null ? cliente.getNombre() : "Mi perfil", "yo", 0.0);
        dto.agregarNodo(nodoYo);

        List<String> visitados = obtenerInmueblesVisitadosPor(idCliente);
        int idx = 0;
        for (String codigo : visitados) {
            Inmueble inm = inmuebleRepositorio.obtenerInmueble(codigo);
            if (inm == null) {
                inm = controlador.buscarInmueblePorCodigo(codigo);
            }
            if (inm == null) {
                continue;
            }

            int otrosClientes = obtenerClientesQueVisitaron(codigo).size();
            String etiqueta = inm.getNombre() != null ? inm.getNombre() : codigo;
            if (otrosClientes > 1) {
                etiqueta += " · " + otrosClientes + " clientes";
            }

            NodoDTO nodo = new NodoDTO(
                    "INM-" + codigo,
                    etiqueta,
                    "visitado",
                    0.8 + (idx * 0.15)
            );
            idx++;
            enriquecerNodoInmueble(nodo, inm);
            dto.agregarNodo(nodo);
            dto.agregarArista(new AristaDTO(nodoYoId, "INM-" + codigo, 1.0));
        }

        List<String> recomendados = recomendarPorComportamiento(idCliente);
        for (String codigo : recomendados) {
            if (visitados.contains(codigo)) {
                continue;
            }
            Inmueble inm = inmuebleRepositorio.obtenerInmueble(codigo);
            if (inm == null) {
                continue;
            }
            NodoDTO nodo = new NodoDTO(
                    "INM-" + codigo,
                    (inm.getNombre() != null ? inm.getNombre() : codigo) + " · sugerido",
                    "disp",
                    1.5 + (idx * 0.1)
            );
            idx++;
            enriquecerNodoInmueble(nodo, inm);
            dto.agregarNodo(nodo);
            dto.agregarArista(new AristaDTO(nodoYoId, "INM-" + codigo, 1.8));
        }

        Set<String> zonasAgregadas = new HashSet<>();
        for (NodoDTO nodo : new ArrayList<>(dto.getNodos())) {
            if (nodo.getZona() == null || zonasAgregadas.contains(nodo.getZona())) {
                continue;
            }
            NodoDTO nodoZona = new NodoDTO(
                    "ZON-" + nodo.getZona(),
                    "Zona " + nodo.getZona(),
                    "zona",
                    nodo.getDistancia() + 0.5
            );
            nodoZona.setZona(nodo.getZona());
            dto.agregarNodo(nodoZona);
            dto.agregarArista(new AristaDTO(nodo.getId(), "ZON-" + nodo.getZona(), 0.4));
            zonasAgregadas.add(nodo.getZona());
        }

        if (cliente.getZonasDeInteres() != null) {
            for (String zona : cliente.getZonasDeInteres()) {
                if (zona == null || zona.isBlank() || zonasAgregadas.contains(zona.trim())) {
                    continue;
                }
                String z = zona.trim();
                NodoDTO nodoZona = new NodoDTO("ZON-" + z, "Interés: " + z, "zona", 2.0);
                nodoZona.setZona(z);
                dto.agregarNodo(nodoZona);
                dto.agregarArista(new AristaDTO(nodoYoId, "ZON-" + z, 1.2));
                zonasAgregadas.add(z);
            }
        }

        agregarFavoritosAlGrafo(dto, cliente, idCliente, nodoYoId, visitados, idx);

        return dto;
    }

    /**
     * Grafo solo con inmuebles guardados en favoritos (todos, sin límite de radio).
     * Conserva el tipo visual: visitado, guardado, disponible o reservado.
     */
    public GrafoDTO obtenerGrafoGuardados(String idCliente) {
        asegurarGrafoCargado();

        GrafoDTO dto = new GrafoDTO();
        Cliente cliente = clienteRepositorio.obtenerClientes().get(idCliente);
        if (cliente == null) {
            return dto;
        }

        String nodoYoId = "yo-" + idCliente;
        NodoDTO nodoYo = new NodoDTO(
                nodoYoId,
                cliente.getNombre() != null ? cliente.getNombre() : "Mi perfil",
                "yo",
                0.0
        );
        dto.agregarNodo(nodoYo);

        List<String> visitados = obtenerInmueblesVisitadosPor(idCliente);
        Set<String> favoritosCodigos = obtenerCodigosFavoritos(cliente);
        Set<String> yaAgregados = new HashSet<>();
        int idx = 0;

        List<Inmueble> favoritos = cliente.getFavoritos() != null
                ? new ArrayList<>(cliente.getFavoritos())
                : new ArrayList<>();

        for (Inmueble fav : favoritos) {
            if (fav == null || fav.getCodigo() == null || fav.getCodigo().isBlank()) {
                continue;
            }
            String codigo = fav.getCodigo().trim();
            if (!yaAgregados.add(codigo)) {
                continue;
            }

            Inmueble inm = resolverInmueble(codigo, fav);
            if (inm == null) {
                continue;
            }

            String tipo = determinarTipoNodo(inm, visitados, favoritosCodigos);
            String etiqueta = (inm.getNombre() != null ? inm.getNombre() : codigo)
                    + " · " + etiquetaEstado(inm);

            NodoDTO nodo = new NodoDTO("INM-" + codigo, etiqueta, tipo, 0.6 + (idx * 0.2));
            idx++;
            enriquecerNodoInmueble(nodo, inm);
            dto.agregarNodo(nodo);
            dto.agregarArista(new AristaDTO(nodoYoId, "INM-" + codigo, 1.0));
        }

        agregarZonasDeNodosInmueble(dto, nodoYoId);
        return dto;
    }

    private void agregarFavoritosAlGrafo(
            GrafoDTO dto,
            Cliente cliente,
            String idCliente,
            String nodoYoId,
            List<String> visitados,
            int idxInicial) {

        if (cliente.getFavoritos() == null || cliente.getFavoritos().isEmpty()) {
            return;
        }

        Set<String> favoritosCodigos = obtenerCodigosFavoritos(cliente);
        Set<String> enGrafo = dto.getNodos().stream()
                .map(NodoDTO::getCodigo)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        int idx = idxInicial;
        for (Inmueble fav : cliente.getFavoritos()) {
            if (fav == null || fav.getCodigo() == null) {
                continue;
            }
            String codigo = fav.getCodigo().trim();
            if (enGrafo.contains(codigo)) {
                continue;
            }

            Inmueble inm = resolverInmueble(codigo, fav);
            if (inm == null) {
                continue;
            }

            String tipo = determinarTipoNodo(inm, visitados, favoritosCodigos);
            NodoDTO nodo = new NodoDTO(
                    "INM-" + codigo,
                    (inm.getNombre() != null ? inm.getNombre() : codigo) + " · guardado",
                    tipo,
                    1.2 + (idx * 0.1)
            );
            idx++;
            enriquecerNodoInmueble(nodo, inm);
            dto.agregarNodo(nodo);
            dto.agregarArista(new AristaDTO(nodoYoId, "INM-" + codigo, 1.5));
            enGrafo.add(codigo);
        }
    }

    private void agregarZonasDeNodosInmueble(GrafoDTO dto, String nodoYoId) {
        Set<String> zonasAgregadas = new HashSet<>();
        for (NodoDTO nodo : new ArrayList<>(dto.getNodos())) {
            if (nodo.getZona() == null || nodo.getTipo().equals("yo") || nodo.getTipo().equals("zona")) {
                continue;
            }
            if (zonasAgregadas.contains(nodo.getZona())) {
                continue;
            }
            NodoDTO nodoZona = new NodoDTO(
                    "ZON-" + nodo.getZona(),
                    "Zona " + nodo.getZona(),
                    "zona",
                    nodo.getDistancia() + 0.4
            );
            nodoZona.setZona(nodo.getZona());
            dto.agregarNodo(nodoZona);
            dto.agregarArista(new AristaDTO(nodo.getId(), "ZON-" + nodo.getZona(), 0.3));
            zonasAgregadas.add(nodo.getZona());
        }
    }

    private Inmueble resolverInmueble(String codigo, Inmueble fallback) {
        Inmueble inm = inmuebleRepositorio.obtenerInmueble(codigo);
        if (inm == null) {
            inm = controlador.buscarInmueblePorCodigo(codigo);
        }
        return inm != null ? inm : fallback;
    }

    private Set<String> obtenerCodigosFavoritos(Cliente cliente) {
        Set<String> codigos = new HashSet<>();
        if (cliente.getFavoritos() == null) {
            return codigos;
        }
        for (Inmueble inm : cliente.getFavoritos()) {
            if (inm != null && inm.getCodigo() != null && !inm.getCodigo().isBlank()) {
                codigos.add(inm.getCodigo().trim());
            }
        }
        return codigos;
    }

    private String etiquetaEstado(Inmueble inm) {
        if (inm.getDisponibilidad() == null) {
            return "N/D";
        }
        return switch (inm.getDisponibilidad()) {
            case DISPONIBLE -> "Disponible";
            case RESERVADO -> "Reservado";
            case NO_DISPONIBLE -> "No disponible";
        };
    }

    private void enriquecerNodoInmueble(NodoDTO nodo, Inmueble inm) {
        nodo.setPrecio(inm.getPrecio());
        nodo.setArea(inm.getArea());
        nodo.setHabitaciones(inm.getNumeroHabitaciones());
        nodo.setZona(inm.getBarrio() != null ? inm.getBarrio() : inm.getCiudad());
        nodo.setCodigo(inm.getCodigo());
        if (inm.getDisponibilidad() != null) {
            nodo.setEstado(inm.getDisponibilidad().name());
        }
    }

    public GrafoDTO obtenerLocacionesCercanas(String idCliente, double radioKm) {
        asegurarGrafoCargado();
        GrafoDTO dto = new GrafoDTO();

        // Buscar cliente usando ClienteRepositorio (que carga desde JSON)
        Map<String, Cliente> clientesMap = clienteRepositorio.obtenerClientes();
        Cliente cliente = clientesMap.get(idCliente);
        
        if (cliente == null) {
            System.out.println("DEBUG GrafoService: Cliente no encontrado con id=" + idCliente);
            System.out.println("DEBUG GrafoService: Clientes disponibles: " + clientesMap.keySet());
            return dto;  // Retorna DTO vacío si no hay cliente
        }

        System.out.println("DEBUG GrafoService: Cliente encontrado: " + cliente.getNombre());

        // Nodo raíz: ubicación del cliente
        NodoDTO nodoYo = new NodoDTO(
            "yo-" + idCliente,
            "Mi ubicación",
            "yo",
            0.0
        );
        dto.agregarNodo(nodoYo);

        List<String> visitados = obtenerInmueblesVisitadosPor(idCliente);
        Set<String> favoritosCodigos = obtenerCodigosFavoritos(cliente);
        List<Inmueble> cercanos = controlador.listarInmuebles();
        System.out.println("DEBUG GrafoService: Total inmuebles en sistema: " + (cercanos != null ? cercanos.size() : 0));

        if (cercanos != null) {
            for (Inmueble inm : cercanos) {
                double distancia = calcularDistanciaSimulada(cliente, inm);
                if (distancia > radioKm) continue;

                String tipo = determinarTipoNodo(inm, visitados, favoritosCodigos);

                NodoDTO nodo = new NodoDTO(
                    "INM-" + inm.getCodigo(),
                    inm.getTipoInmueble() + " · " + inm.getBarrio(),
                    tipo,
                    distancia
                );
                nodo.setPrecio(inm.getPrecio());
                nodo.setArea(inm.getArea());
                nodo.setHabitaciones(inm.getNumeroHabitaciones());
                nodo.setZona(inm.getBarrio());
                nodo.setCodigo(inm.getCodigo());
                nodo.setEstado(inm.getDisponibilidad().name());

                dto.agregarNodo(nodo);
                dto.agregarArista(new AristaDTO("yo-" + idCliente, "INM-" + inm.getCodigo(), distancia));
            }
        }

        // Agregar nodos de zona
        Set<String> zonasAgregadas = new HashSet<>();
        for (NodoDTO nodo : dto.getNodos()) {
            if (nodo.getZona() != null && !zonasAgregadas.contains(nodo.getZona())) {
                NodoDTO nodoZona = new NodoDTO(
                    "ZON-" + nodo.getZona(),
                    "Zona " + nodo.getZona(),
                    "zona",
                    nodo.getDistancia()
                );
                nodoZona.setZona(nodo.getZona());
                dto.agregarNodo(nodoZona);
                dto.agregarArista(new AristaDTO("INM-" + nodo.getCodigo(), "ZON-" + nodo.getZona(), 0.3));
                zonasAgregadas.add(nodo.getZona());
            }
        }

        agregarFavoritosAlGrafo(dto, cliente, idCliente, "yo-" + idCliente, visitados, cercanos != null ? cercanos.size() : 0);

        System.out.println("DEBUG GrafoService: DTO final con " + dto.getNodos().size() + " nodos y " + dto.getAristas().size() + " aristas");

        return dto;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. RECOMENDACIÓN POR GRAFO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Sugiere inmuebles basándose en lo que visitaron clientes similares.
     * Usa BFS sobre el grafo cliente-inmueble para encontrar inmuebles
     * visitados por clientes con perfil parecido al dado.
     */
    public List<String> recomendarPorComportamiento(String idCliente) {
        asegurarGrafoCargado();
        List<String> visitadosPorEsteCliente = obtenerInmueblesVisitadosPor(idCliente);
        Map<String, Integer> frecuencia = new HashMap<>();

        // BFS: clientes → inmuebles en común → otros clientes → sus inmuebles
        for (String codigoInm : visitadosPorEsteCliente) {
            String nodoInm = "INM-" + codigoInm;
            List<String> otrosClientes = grafoClienteInmueble.obtenerVecinos(nodoInm);

            for (String otroNodo : otrosClientes) {
                if (otroNodo.equals("CLI-" + idCliente)) continue;
                List<String> susInmuebles = grafoClienteInmueble.obtenerVecinos(otroNodo);

                for (String inm : susInmuebles) {
                    if (!visitadosPorEsteCliente.contains(inm.replace("INM-", ""))) {
                        frecuencia.merge(inm, 1, Integer::sum);
                    }
                }
            }
        }

        // Ordenar por frecuencia descendente y devolver los top 5
        return frecuencia.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(e -> e.getKey().replace("INM-", ""))
            .toList();
    }

    /**
     * Detecta inmuebles con alta demanda contando vecinos en el grafo.
     */
    public List<String> detectarInmueblesAltaDemanda(int umbral) {
        asegurarGrafoCargado();
        List<String> resultado = new ArrayList<>();
        for (String nodo : grafoClienteInmueble.obtenerNodos()) {
            if (nodo.startsWith("INM-")) {
                int visitas = grafoClienteInmueble.obtenerVecinos(nodo).size();
                if (visitas >= umbral) {
                    resultado.add(nodo.replace("INM-", ""));
                }
            }
        }
        return resultado;
    }

    /**
     * Detecta zonas con concentración inusual de interés.
     */
    public Map<String, Integer> rankingZonasPorActividad() {
        asegurarGrafoCargado();
        Map<String, Integer> ranking = new HashMap<>();
        for (String nodo : grafoInmuebleZona.obtenerNodos()) {
            if (nodo.startsWith("ZON-")) {
                int conexiones = grafoInmuebleZona.obtenerVecinos(nodo).size();
                ranking.put(nodo.replace("ZON-", ""), conexiones);
            }
        }
        return ranking;
    }

    /**
     * Devuelve los inmuebles que visitó un cliente específico.
     */
    public List<String> obtenerInmueblesVisitadosPor(String idCliente) {
        String nodoCliente = "CLI-" + idCliente;
        List<String> vecinos = grafoClienteInmueble.obtenerVecinos(nodoCliente);
        List<String> resultado = new ArrayList<>();
        for (String v : vecinos) {
            if (v.startsWith("INM-")) resultado.add(v.replace("INM-", ""));
        }
        return resultado;
    }

    /**
     * Devuelve qué clientes visitaron un inmueble específico.
     */
    public List<String> obtenerClientesQueVisitaron(String codigoInmueble) {
        String nodoInm = "INM-" + codigoInmueble;
        List<String> vecinos = grafoClienteInmueble.obtenerVecinos(nodoInm);
        List<String> resultado = new ArrayList<>();
        for (String v : vecinos) {
            if (v.startsWith("CLI-")) resultado.add(v.replace("CLI-", ""));
        }
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════════

    private String determinarTipoNodo(Inmueble inm, List<String> visitados, Set<String> favoritosCodigos) {
        String codigo = inm.getCodigo() != null ? inm.getCodigo().trim() : "";
        if (!codigo.isEmpty() && visitados.contains(codigo)) {
            return "visitado";
        }
        if (inm.getDisponibilidad() != null) {
            if (inm.getDisponibilidad() == Inmueble.Disponibilidad.RESERVADO
                    || inm.getDisponibilidad() == Inmueble.Disponibilidad.NO_DISPONIBLE) {
                return "reservado";
            }
        }
        if (!codigo.isEmpty() && favoritosCodigos.contains(codigo)) {
            return "guardado";
        }
        if (inm.getDisponibilidad() == null) {
            return "disp";
        }
        return inm.getDisponibilidad() == Inmueble.Disponibilidad.DISPONIBLE ? "disp" : "reservado";
    }

    /**
     * Simula distancia en km basada en zona.
     * En producción aquí iría el cálculo con coordenadas reales (Haversine).
     */
    private double calcularDistanciaSimulada(Cliente cliente, Inmueble inm) {
        List<String> zonasCliente = cliente.getZonasDeInteres();
        if (zonasCliente != null && zonasCliente.contains(inm.getBarrio())) {
            return 0.5 + Math.random() * 2.0;
        }
        return 2.0 + Math.random() * 6.0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. DTOs (clases internas para serialización al frontend)
    // ═══════════════════════════════════════════════════════════════════════════

    public static class GrafoDTO {
        private final List<NodoDTO>  nodos   = new ArrayList<>();
        private final List<AristaDTO> aristas = new ArrayList<>();

        public void agregarNodo(NodoDTO n)    { nodos.add(n); }
        public void agregarArista(AristaDTO a){ aristas.add(a); }
        public List<NodoDTO>  getNodos()      { return nodos; }
        public List<AristaDTO> getAristas()   { return aristas; }
    }

    public static class NodoDTO {
        private String id, label, tipo, zona, codigo, estado;
        private double distancia, precio, area;
        private int    habitaciones;

        public NodoDTO(String id, String label, String tipo, double distancia) {
            this.id = id; this.label = label;
            this.tipo = tipo; this.distancia = distancia;
        }

        // Getters y setters
        public String getId()           { return id; }
        public String getLabel()        { return label; }
        public String getTipo()         { return tipo; }
        public String getZona()         { return zona; }
        public String getCodigo()       { return codigo; }
        public String getEstado()       { return estado; }
        public double getDistancia()    { return distancia; }
        public double getPrecio()       { return precio; }
        public double getArea()         { return area; }
        public int    getHabitaciones() { return habitaciones; }

        public void setZona(String zona)             { this.zona = zona; }
        public void setCodigo(String codigo)         { this.codigo = codigo; }
        public void setEstado(String estado)         { this.estado = estado; }
        public void setPrecio(double precio)         { this.precio = precio; }
        public void setArea(double area)             { this.area = area; }
        public void setHabitaciones(int hab)         { this.habitaciones = hab; }
    }

    public static class AristaDTO {
        private final String origen, destino;
        private final double peso;

        public AristaDTO(String origen, String destino, double peso) {
            this.origen = origen; this.destino = destino; this.peso = peso;
        }

        public String getOrigen()  { return origen; }
        public String getDestino() { return destino; }
        public double getPeso()    { return peso; }
    }
}