package com.uniquindio.Service;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Controller.ControladorPrincipal;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
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

    // ─── Referencia al controlador principal ─────────────────────────────────
    private final ControladorPrincipal controlador;

    private GrafoService() {
        this.grafoClienteInmueble = new Grafo<>();
        this.grafoInmuebleZona    = new Grafo<>();
        this.grafoZonaCliente     = new Grafo<>();
        this.controlador          = ControladorPrincipal.getInstancia();
        this.clienteRepositorio   = new ClienteRepositorio();
        this.inmuebleRepositorio  = new InmuebleRepositorio();
    }

    public static GrafoService getInstancia() {
        if (instancia == null) instancia = new GrafoService();
        return instancia;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. REGISTRO DE RELACIONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra que un cliente visitó un inmueble.
     * Se llama desde VisitaService cuando una visita pasa a REALIZADA.
     */
    public void registrarVisita(String idCliente, String codigoInmueble) {
        String nodoCliente  = "CLI-" + idCliente;
        String nodoInmueble = "INM-" + codigoInmueble;

        grafoClienteInmueble.agregarNodo(nodoCliente);
        grafoClienteInmueble.agregarNodo(nodoInmueble);
        grafoClienteInmueble.agregarArista(nodoCliente, nodoInmueble);

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
    public GrafoDTO obtenerLocacionesCercanas(String idCliente, double radioKm) {
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

        // Buscar inmuebles relacionados via grafo
        List<String> visitados  = obtenerInmueblesVisitadosPor(idCliente);
        List<Inmueble> cercanos  = controlador.listarInmuebles();
        System.out.println("DEBUG GrafoService: Total inmuebles en sistema: " + (cercanos != null ? cercanos.size() : 0));

        if (cercanos != null) {
            for (Inmueble inm : cercanos) {
                double distancia = calcularDistanciaSimulada(cliente, inm);
                if (distancia > radioKm) continue;

                String tipo = determinarTipoNodo(inm, visitados);

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

    private String determinarTipoNodo(Inmueble inm, List<String> visitados) {
        if (visitados.contains(inm.getCodigo())) return "visitado";
        return switch (inm.getDisponibilidad().name()) {
            case "DISPONIBLE"    -> "disp";
            case "RESERVADO"     -> "reservado";
            case "NO_DISPONIBLE" -> "reservado";
            default              -> "disp";
        };
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