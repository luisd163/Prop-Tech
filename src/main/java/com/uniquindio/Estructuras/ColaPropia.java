package com.uniquindio.Estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Cola (Queue) genérica — implementada sobre LinkedList propia.
 * Principio FIFO: First In, First Out.
 *
 * Usos en PropTech:
 *   - Solicitudes de atención de clientes
 *   - Visitas pendientes por procesar
 *   - Tareas administrativas en espera
 *   - Alertas pendientes de revisión
 */
public class ColaPropia<T> implements Iterable<T> {

    private final LinkedListPropia<T> lista;

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    public ColaPropia() {
        this.lista = new LinkedListPropia<>();
    }

    // ─────────────────────────────────────────────
    // Operaciones principales
    // ─────────────────────────────────────────────

    /**
     * Encola un elemento al final.
     * Complejidad: O(n)
     */
    public void encolar(T dato) {
        lista.agregarAlFinal(dato);
    }

    /**
     * Desencola el elemento del frente.
     * Complejidad: O(1)
     */
    public T desencolar() {

        if (estaVacia()) {
            throw new RuntimeException(
                    "Cola vacía"
            );
        }

        return lista.eliminarAlInicio();
    }

    /**
     * Retorna el frente sin eliminarlo.
     * Complejidad: O(1)
     */
    public T frente() {

        if (estaVacia()) {
            throw new RuntimeException(
                    "Cola vacía"
            );
        }

        return lista.verPrimero();
    }

    /**
     * Retorna el último elemento.
     * Complejidad: O(n)
     */
    public T fondo() {

        if (estaVacia()) {
            throw new RuntimeException(
                    "Cola vacía"
            );
        }

        return lista.verUltimo();
    }

    // ─────────────────────────────────────────────
    // Métodos de consulta
    // ─────────────────────────────────────────────

    /**
     * Busca el primer elemento que cumpla una condición.
     * Complejidad: O(n)
     */
    public T buscar(Predicate<T> condicion) {
        return lista.buscar(condicion);
    }

    /**
     * Verifica si existe un elemento.
     * Complejidad: O(n)
     */
    public boolean contiene(T dato) {
        return lista.contiene(dato);
    }

    /**
     * Elimina un elemento específico.
     * Complejidad: O(n)
     */
    public boolean eliminar(T dato) {
        return lista.eliminar(dato);
    }

    /**
     * Cuenta elementos que cumplan condición.
     * Complejidad: O(n)
     */
    public int contar(Predicate<T> condicion) {
        return lista.contar(condicion);
    }

    /**
     * Ejecuta una acción sobre cada elemento.
     */
    @Override
    public void forEach(Consumer<? super T> accion) {

        for (T dato : this) {
            accion.accept(dato);
        }
    }

    // ─────────────────────────────────────────────
    // Estado
    // ─────────────────────────────────────────────

    public boolean estaVacia() {
        return lista.estaVacia();
    }

    public int tamanio() {
        return lista.tamanio();
    }

    public void limpiar() {
        lista.limpiar();
    }

    // ─────────────────────────────────────────────
    // Conversión
    // ─────────────────────────────────────────────

    /**
     * Retorna la cola como ArrayList.
     * El índice 0 representa el frente.
     */
    public ArrayListPropio<T> verTodos() {
        return lista.aArrayList();
    }

    /**
     * Convierte la cola a LinkedList.
     */
    public LinkedListPropia<T> aLinkedList() {
        return lista;
    }

    /**
     * Retorna una copia invertida.
     * Útil para reportes cronológicos inversos.
     */
    public ColaPropia<T> reversa() {

        ColaPropia<T> invertida = new ColaPropia<>();

        ArrayListPropio<T> elementos = lista.aArrayList();  
        for (int i = elementos.tamanio() - 1; i >= 0; i--) {
            invertida.encolar(elementos.obtener(i));
        }

        return invertida;
    }

    // ─────────────────────────────────────────────
    // Iterable
    // ─────────────────────────────────────────────

    @Override
    public Iterator<T> iterator() {
        return new ColaIterator();
    }

    /**
     * Iterador FIFO:
     * Frente → Fondo
     */
    private class ColaIterator
            implements Iterator<T> {

        private final Iterator<T> iteradorLista =
                lista.iterator();

        @Override
        public boolean hasNext() {
            return iteradorLista.hasNext();
        }

        @Override
        public T next() {

            if (!hasNext()) {

                throw new NoSuchElementException(
                        "No hay más elementos"
                );
            }

            return iteradorLista.next();
        }
    }

    // ─────────────────────────────────────────────
    // Visualización
    // ─────────────────────────────────────────────

    @Override
    public String toString() {

        if (estaVacia()) {
            return "[ cola vacía ]";
        }

        StringBuilder sb =
                new StringBuilder();

        sb.append("FRENTE -> ");

        for (T dato : this) {
            sb.append(dato).append(" -> ");
        }

        sb.append("FONDO");

        return sb.toString();
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// COLA DE PRIORIDAD
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Cola de Prioridad genérica.
 *
 * Implementación:
 *   ArrayList ordenado por prioridad.
 *
 * Inserción: O(n)
 * Extracción: O(1)
 *
 * Usos en PropTech:
 *   - Clientes VIP
 *   - Alertas críticas
 *   - Contratos urgentes
 *   - Prioridad de visitas
 */
class ColaPrioridad<T>
        implements Iterable<ColaPrioridad.EntradaPrioridad<T>> {

    private final ArrayListPropio<EntradaPrioridad<T>> lista;

    private final Comparator<EntradaPrioridad<T>>
            comparador;

    // ─────────────────────────────────────────────
    // Clase interna
    // ─────────────────────────────────────────────

    public static class EntradaPrioridad<T>
            implements Comparable<EntradaPrioridad<T>> {

        private final T dato;

        private final int prioridad;

        public EntradaPrioridad(
                T dato,
                int prioridad
        ) {

            this.dato = dato;

            this.prioridad = prioridad;
        }

        public T getDato() {
            return dato;
        }

        public int getPrioridad() {
            return prioridad;
        }

        @Override
        public int compareTo(
                EntradaPrioridad<T> otra
        ) {

            return Integer.compare(
                    otra.prioridad,
                    this.prioridad
            );
        }

        @Override
        public String toString() {

            return "[P"
                    + prioridad
                    + "] "
                    + dato;
        }
    }

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    public ColaPrioridad() {

        this.lista = new ArrayListPropio<>();

        this.comparador =
                EntradaPrioridad::compareTo;
    }

    // ─────────────────────────────────────────────
    // Operaciones principales
    // ─────────────────────────────────────────────

    /**
     * Inserta elemento con prioridad.
     * Complejidad: O(n)
     */
    public void insertar(
            T dato,
            int prioridad
    ) {

        EntradaPrioridad<T> nueva =
                new EntradaPrioridad<>(
                        dato,
                        prioridad
                );

        lista.agregar(nueva);

        lista.ordenar(comparador);
    }

    /**
     * Extrae el elemento con mayor prioridad.
     * Complejidad: O(1)
     */
    public T extraer() {

        if (estaVacia()) {

            throw new RuntimeException(
                    "Cola de prioridad vacía"
            );
        }

        return lista.eliminar(0)
                .getDato();
    }

    /**
     * Retorna el elemento de mayor prioridad.
     */
    public T verMayorPrioridad() {

        if (estaVacia()) {

            throw new RuntimeException(
                    "Cola de prioridad vacía"
            );
        }

        return lista.obtener(0)
                .getDato();
    }

    /**
     * Retorna prioridad del frente.
     */
    public int prioridadAlFrente() {

        if (estaVacia()) {

            throw new RuntimeException(
                    "Cola de prioridad vacía"
            );
        }

        return lista.obtener(0)
                .getPrioridad();
    }

    /**
     * Actualiza prioridad de un elemento.
     * Complejidad: O(n)
     */
    public boolean actualizarPrioridad(
            T dato,
            int nuevaPrioridad
    ) {

        for (int i = 0;
             i < lista.tamanio();
             i++) {

            EntradaPrioridad<T> actual =
                    lista.obtener(i);

            if (actual.getDato().equals(dato)) {

                lista.eliminar(i);

                insertar(
                        dato,
                        nuevaPrioridad
                );

                return true;
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────
    // Métodos de consulta
    // ─────────────────────────────────────────────

    /**
     * Verifica si existe un dato.
     */
    public boolean contiene(T dato) {

        for (EntradaPrioridad<T> entrada : this) {

            if (entrada.getDato()
                    .equals(dato)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Busca una entrada por condición.
     */
    public EntradaPrioridad<T> buscar(
            Predicate<EntradaPrioridad<T>>
                    condicion
    ) {

        for (EntradaPrioridad<T> entrada : this) {

            if (condicion.test(entrada)) {
                return entrada;
            }
        }

        return null;
    }

    /**
     * Elimina un elemento específico.
     */
    public boolean eliminar(T dato) {

        for (int i = 0;
             i < lista.tamanio();
             i++) {

            if (lista.obtener(i)
                    .getDato()
                    .equals(dato)) {

                lista.eliminar(i);

                return true;
            }
        }

        return false;
    }

    /**
     * Ejecuta acción sobre cada entrada.
     */
    public void forEach(Consumer<? super EntradaPrioridad<T>>accion) {

        for (EntradaPrioridad<T> e : this) {
            accion.accept(e);
        }
    }

    // ─────────────────────────────────────────────
    // Estado
    // ─────────────────────────────────────────────

    public boolean estaVacia() {
        return lista.estaVacia();
    }

    public int tamanio() {
        return lista.tamanio();
    }

    public void limpiar() {
        lista.limpiar();
    }

    // ─────────────────────────────────────────────
    // Conversión
    // ─────────────────────────────────────────────

    /**
     * Retorna todos los elementos.
     */
    public ArrayListPropio<EntradaPrioridad<T>>
    verTodos() {

        return lista;
    }

    // ─────────────────────────────────────────────
    // Iterable
    // ─────────────────────────────────────────────

    @Override
    public Iterator<EntradaPrioridad<T>>
    iterator() {

        return new ColaPrioridadIterator();
    }

    private class ColaPrioridadIterator
            implements Iterator<EntradaPrioridad<T>> {

        private final Iterator<
                EntradaPrioridad<T>>
                iterador = lista.iterator();

        @Override
        public boolean hasNext() {
            return iterador.hasNext();
        }

        @Override
        public EntradaPrioridad<T> next() {

            if (!hasNext()) {

                throw new NoSuchElementException(
                        "No hay más elementos"
                );
            }

            return iterador.next();
        }
    }

    // ─────────────────────────────────────────────
    // Visualización
    // ─────────────────────────────────────────────

    @Override
    public String toString() {

        if (estaVacia()) {
            return "[ cola prioridad vacía ]";
        }

        StringBuilder sb =
                new StringBuilder();

        sb.append("PRIORIDAD -> ");

        for (EntradaPrioridad<T> e : this) {

            sb.append(e)
                    .append(" -> ");
        }

        sb.append("FIN");

        return sb.toString();
    }
}