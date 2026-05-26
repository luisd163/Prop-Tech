package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Visita;
import com.uniquindio.Model.Operacion;
import com.uniquindio.Model.Alerta;
import com.uniquindio.Estructuras.TablaHash;
import com.uniquindio.Estructuras.LinkedListPropia;
import com.uniquindio.Estructuras.ColaPrioridadPropia;
import com.uniquindio.Estructuras.ArbolBSTPropio;
import com.uniquindio.Estructuras.ArrayListPropio;
import com.uniquindio.Estructuras.ColaPropia;
import com.uniquindio.Estructuras.Stack;

import java.util.ArrayList;
import java.util.List;

public class ControladorPrincipal {

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static ControladorPrincipal instancia;

    // ─── Estructuras de datos ─────────────────────────────────────────────────

    // TablaHash para búsqueda O(1) por identificador
    private final TablaHash<String, Cliente>   clientesPorId;
    private final TablaHash<String, Inmueble>  inmueblesPorCodigo;
    private final TablaHash<String, Asesor>    asesoresPorId;

    // Listas enlazadas para historial y colecciones ordenadas
    private final LinkedListPropia<Cliente>       listaClientes;
    private final LinkedListPropia<Inmueble>      listaInmuebles;
    private final LinkedListPropia<Asesor>        listaAsesores;
    private final LinkedListPropia<Operacion>     listaOperaciones;

    // Árbol BST para ordenamiento por precio
    private final ArbolBSTPropio<Inmueble>           arbolInmueblesPorPrecio;

    // ColaPropia de visitas pendientes
    private final ColaPropia<Visita>                 ColaPropiaVisitas;

    // ColaPropia de prioridad para alertas
    private final ColaPrioridadPropia<Alerta>        ColaPropiaAlertas;

    // Stack para deshacer cambios
    private final Stack<String>                 StackAcciones;

    // ─── Constructor privado ──────────────────────────────────────────────────
    private ControladorPrincipal() {
        this.clientesPorId          = new TablaHash<>();
        this.inmueblesPorCodigo     = new TablaHash<>();
        this.asesoresPorId          = new TablaHash<>();
        this.listaClientes          = new LinkedListPropia<>();
        this.listaInmuebles         = new LinkedListPropia<>();
        this.listaAsesores          = new LinkedListPropia<>();
        this.listaOperaciones       = new LinkedListPropia<>();
        this.arbolInmueblesPorPrecio = new ArbolBSTPropio<>(
            (a, b) -> Double.compare(a.getPrecio(), b.getPrecio())
        );        
        this.ColaPropiaVisitas            = new ColaPropia<>();
        this.ColaPropiaAlertas            = new ColaPrioridadPropia<>();
        this.StackAcciones           = new Stack<>();
    }

    public static ControladorPrincipal getInstancia() {
        if (instancia == null) instancia = new ControladorPrincipal();
        return instancia;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS USADOS POR GrafoService
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── 1. buscarInmueblePorCodigo ───────────────────────────────────────────
    /**
     * Busca un inmueble por su código único.
     * Usa TablaHash → O(1) promedio.
     * Usado en GrafoService.registrarVisita() para obtener el barrio
     * del inmueble y registrar la relación inmueble-zona en el grafo.
     */
    public Inmueble buscarInmueblePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        return inmueblesPorCodigo.buscar(codigo.trim());
    }

    // ─── 2. buscarClientePorId ────────────────────────────────────────────────
    /**
     * Busca un cliente por su identificación.
     * Usa TablaHash → O(1) promedio.
     * Usado en GrafoService.obtenerLocacionesCercanas() para obtener
     * las zonas de interés del cliente y calcular distancias simuladas.
     */
    public Cliente buscarClientePorId(String id) {
        if (id == null || id.isBlank()) return null;
        return clientesPorId.buscar(id.trim());
    }

    // ─── 3. listarInmuebles ───────────────────────────────────────────────────
    /**
     * Devuelve todos los inmuebles registrados en el sistema.
     * Recorre la LinkedListPropia → O(n).
     * Usado en GrafoService.obtenerLocacionesCercanas() para construir
     * los nodos del grafo de locaciones cercanas al cliente.
     */
    public List<Inmueble> listarInmuebles() {
        List<Inmueble> resultado = new ArrayList<>();
        listaInmuebles.forEach(resultado::add);
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE GESTIÓN DE INMUEBLES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra un nuevo inmueble en el sistema.
     * Lo agrega a la TablaHash (búsqueda rápida), a la LinkedListPropia
     * (historial) y al ArbolBSTPropio (ordenamiento por precio).
     */
    public boolean registrarInmueble(Inmueble inmueble) {
        if (inmueble == null || inmueble.getCodigo() == null) return false;
        if (inmueblesPorCodigo.buscar(inmueble.getCodigo()) != null) return false;

        inmueblesPorCodigo.insertar(inmueble.getCodigo(), inmueble);
        listaInmuebles.agregarAlFinal(inmueble);
        arbolInmueblesPorPrecio.insertar(inmueble);
        StackAcciones.push("REGISTRO_INMUEBLE:" + inmueble.getCodigo());
        return true;
    }

    /**
     * Modifica los datos de un inmueble existente.
     */
    public boolean modificarInmueble(String codigo, Inmueble datosNuevos) {
        Inmueble existente = inmueblesPorCodigo.buscar(codigo);
        if (existente == null) return false;

        StackAcciones.push("MODIFICACION_INMUEBLE:" + codigo);

        existente.setDireccion(datosNuevos.getDireccion());
        existente.setCiudad(datosNuevos.getCiudad());
        existente.setBarrio(datosNuevos.getBarrio());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setArea(datosNuevos.getArea());
        existente.setNumeroHabitaciones(datosNuevos.getNumeroHabitaciones());
        existente.setNumeroBanos(datosNuevos.getNumeroBanos());
        existente.setDisponibilidad(datosNuevos.getDisponibilidad());

        // Reconstruir árbol porque el precio (clave de orden) puede haber cambiado
        reconstruirArbolInmuebles();
        return true;
    }

    /**
     * Elimina un inmueble del sistema.
     */
    public boolean eliminarInmueble(String codigo) {
        Inmueble inmueble = inmueblesPorCodigo.buscar(codigo);
        if (inmueble == null) return false;

        StackAcciones.push("ELIMINACION_INMUEBLE:" + codigo);
        inmueblesPorCodigo.eliminar(codigo);
        listaInmuebles.eliminarSi(i -> i.getCodigo().equals(codigo));
        reconstruirArbolInmuebles();
        return true;
    }

    /**
     * Devuelve inmuebles dentro de un rango de precios.
     * Usa el ArbolBSTPropio → O(log n) promedio.
     */
    public List<Inmueble> buscarInmueblesPorRangoPrecio(double min, double max) {
        List<Inmueble> resultado = new ArrayList<>();
        listaInmuebles.forEach(inmueble -> {
            if (inmueble.getPrecio() >= min && inmueble.getPrecio() <= max) {
                resultado.add(inmueble);
            }
        });
        return resultado;
    }

    /**
     * Devuelve todos los inmuebles ordenados por precio ascendente.
     * Usa recorrido inorden del ArbolBSTPropio → O(n).
     */
    public ArrayListPropio<Inmueble> listarInmueblesOrdenadosPorPrecio() {
        return arbolInmueblesPorPrecio.inorden();
    }

    /**
     * Reconstruye el árbol BST de inmuebles desde la lista enlazada.
     * Se llama después de modificar o eliminar inmuebles.
     */
    private void reconstruirArbolInmuebles() {
        arbolInmueblesPorPrecio.limpiar();
        listaInmuebles.forEach(arbolInmueblesPorPrecio::insertar);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE GESTIÓN DE CLIENTES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra un nuevo cliente.
     */
    public boolean registrarCliente(Cliente cliente) {
        if (cliente == null || cliente.getIdentificacion() == null) return false;
        if (clientesPorId.buscar(cliente.getIdentificacion()) != null) return false;

        clientesPorId.insertar(cliente.getIdentificacion(), cliente);
        listaClientes.agregarAlFinal(cliente);
        return true;
    }

    /**
     * Modifica los datos de un cliente existente.
     */
    public boolean modificarCliente(String id, Cliente datosNuevos) {
        Cliente existente = clientesPorId.buscar(id);
        if (existente == null) return false;

        existente.setNombre(datosNuevos.getNombre());
        existente.setCorreo(datosNuevos.getCorreo());
        existente.setTelefono(datosNuevos.getTelefono());
        existente.setPresupuesto(datosNuevos.getPresupuesto());
        existente.setZonasDeInteres(datosNuevos.getZonasDeInteres());
        existente.setTipoInmuebleDeseado(datosNuevos.getTipoInmuebleDeseado());
        existente.setCantidadMinimaHabitaciones(datosNuevos.getCantidadMinimaHabitaciones());
        existente.setEstadoBusqueda(datosNuevos.getEstadoBusqueda());
        return true;
    }

    /**
     * Elimina un cliente del sistema.
     */
    public boolean eliminarCliente(String id) {
        if (clientesPorId.buscar(id) == null) return false;
        clientesPorId.eliminar(id);
        listaClientes.eliminarSi(c -> c.getIdentificacion().equals(id));
        return true;
    }

    /**
     * Devuelve todos los clientes del sistema.
     */
    public List<Cliente> listarClientes() {
        List<Cliente> resultado = new ArrayList<>();
        listaClientes.forEach(resultado::add);
        return resultado;
    }

    /**
     * Valida las credenciales de un cliente para el login.
     */
    public boolean validarCredenciales(String email, String password) {
        List<Cliente> clientes = listarClientes();
        for (Cliente c : clientes) {
            if (c.getCorreo().equals(email) && c.getContrasena().equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca un cliente por su correo electrónico.
     */
    public Cliente buscarClientePorCorreo(String correo) {
        List<Cliente> clientes = listarClientes();
        for (Cliente c : clientes) {
            if (c.getCorreo().equalsIgnoreCase(correo)) return c;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE GESTIÓN DE ASESORES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra un nuevo asesor.
     */
    public boolean registrarAsesor(Asesor asesor) {
        if (asesor == null || asesor.getIdentificacion() == null) return false;
        if (asesoresPorId.buscar(asesor.getIdentificacion()) != null) return false;

        asesoresPorId.insertar(asesor.getIdentificacion(), asesor);
        listaAsesores.agregarAlFinal(asesor);
        return true;
    }

    /**
     * Busca un asesor por su identificación.
     */
    public Asesor buscarAsesorPorId(String id) {
        if (id == null || id.isBlank()) return null;
        return asesoresPorId.buscar(id.trim());
    }

    /**
     * Devuelve todos los asesores del sistema.
     */
    public List<Asesor> listarAsesores() {
        List<Asesor> resultado = new ArrayList<>();
        listaAsesores.forEach(resultado::add);
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE VISITAS Y OPERACIONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Agrega una visita a la ColaPropia de pendientes.
     */
    public void enColaPropiarVisita(Visita visita) {
        if (visita != null) ColaPropiaVisitas.encolar(visita);
    }

    /**
     * Extrae la siguiente visita de la ColaPropia.
     */
    public Visita siguienteVisita() {
        return ColaPropiaVisitas.desencolar();
    }

    /**
     * Registra una operación completada.
     */
    public void registrarOperacion(Operacion operacion) {
        if (operacion != null) listaOperaciones.agregarAlFinal(operacion);
    }

    /**
     * Devuelve todas las operaciones del sistema.
     */
    public List<Operacion> listarOperaciones() {
        List<Operacion> resultado = new ArrayList<>();
        listaOperaciones.forEach(resultado::add);
        return resultado;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS DE ALERTAS Y ACCIONES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Agrega una alerta a la ColaPropia de prioridad.
     */
    public void agregarAlerta(Alerta alerta) {
        if (alerta != null) ColaPropiaAlertas.insertar(alerta, alerta.getNivel().ordinal());
    }

    /**
     * Extrae la alerta más urgente.
     */
    public Alerta alertaMasUrgente() {
        return ColaPropiaAlertas.extraer();
    }

}