package com.uniquindio.Estructuras;

import java.util.Comparator;

/**
 * Cola de Prioridad genérica.
 * Implementación basada en ArrayList propio (ordenada internamente).
 *
 * PRINCIPIO:
 *   - Mayor prioridad sale primero
 *   - Inserción mantiene orden
 *
 * USOS EN PROPTTECH:
 *   - Clientes VIP primero
 *   - Alertas críticas de contratos
 *   - Inmuebles más demandados
 *   - Tareas urgentes del sistema
 */
public class ColaPrioridadPropia<T> {

    // ─────────────────────────────────────────────
    // ENTRADA (dato + prioridad)
    // ─────────────────────────────────────────────

    public static class EntradaPrioridad<T> {
        public T dato;
        public int prioridad;

        public EntradaPrioridad(T dato, int prioridad) {
            this.dato = dato;
            this.prioridad = prioridad;
        }

        @Override
        public String toString() {
            return "[P" + prioridad + "] " + dato;
        }
    }

    // ─────────────────────────────────────────────
    // ESTRUCTURA INTERNA
    // ─────────────────────────────────────────────

    private final ArrayListPropio<EntradaPrioridad<T>> lista;

    // Orden: mayor prioridad primero
    private final Comparator<EntradaPrioridad<T>> comparador =
            (a, b) -> Integer.compare(b.prioridad, a.prioridad);

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public ColaPrioridadPropia() {
        this.lista = new ArrayListPropio<>();
    }

    // ─────────────────────────────────────────────
    // OPERACIONES PRINCIPALES
    // ─────────────────────────────────────────────

    /**
     * Inserta un elemento con prioridad.
     * Mantiene orden automático.
     * Complejidad: O(n)
     */
    public void insertar(T dato, int prioridad) {
        EntradaPrioridad<T> nuevo = new EntradaPrioridad<>(dato, prioridad);

        lista.agregar(nuevo);
        lista.ordenar(comparador);
    }

    /**
     * Extrae el elemento de mayor prioridad.
     * Complejidad: O(1)
     */
    public T extraer() {
        if (estaVacia()) {
            throw new RuntimeException("Cola de prioridad vacía");
        }
        return lista.eliminar(0).dato;
    }

    /**
     * Ver el elemento con mayor prioridad sin eliminarlo.
     */
    public T verPrimero() {
        if (estaVacia()) {
            throw new RuntimeException("Cola de prioridad vacía");
        }
        return lista.obtener(0).dato;
    }

    /**
     * Ver prioridad del primero.
     */
    public int prioridadPrimero() {
        if (estaVacia()) {
            throw new RuntimeException("Cola de prioridad vacía");
        }
        return lista.obtener(0).prioridad;
    }

    // ─────────────────────────────────────────────
    // MODIFICACIÓN
    // ─────────────────────────────────────────────

    /**
     * Actualiza prioridad de un elemento.
     * Complejidad: O(n)
     */
    public boolean actualizarPrioridad(T dato, int nuevaPrioridad) {

        for (int i = 0; i < lista.tamanio(); i++) {

            if (lista.obtener(i).dato.equals(dato)) {
                lista.eliminar(i);
                insertar(dato, nuevaPrioridad);
                return true;
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────
    // ESTADO
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
    // VISUALIZACIÓN
    // ─────────────────────────────────────────────

    @Override
    public String toString() {
        return "ColaPrioridad (mayor → menor): " + lista.toString();
    }
}