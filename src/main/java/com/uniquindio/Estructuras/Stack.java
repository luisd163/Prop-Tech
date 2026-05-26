package com.uniquindio.Estructuras;

/**
 * Pila (Stack) genérica basada en LinkedList propia.
 * Principio LIFO: Last In, First Out.
 *
 * USOS EN PROPTTECH:
 *   - Historial de acciones (undo)
 *   - Navegación inversa
 *   - Evaluación de procesos reversibles
 *   - Control de operaciones recientes
 */
public class Stack<T> {

    private final LinkedListPropia<T> lista;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public Stack() {
        this.lista = new LinkedListPropia<>();
    }

    // ─────────────────────────────────────────────
    // OPERACIONES PRINCIPALES
    // ─────────────────────────────────────────────

    /**
     * Inserta un elemento en la cima de la pila.
     * Complejidad: O(1)
     */
    public void push(T dato) {
        lista.agregarAlInicio(dato);
    }

    /**
     * Elimina y retorna el elemento de la cima.
     * Complejidad: O(1)
     */
    public T pop() {
        if (estaVacia()) {
            throw new RuntimeException("Pila vacía");
        }
        return lista.eliminarAlInicio();
    }

    /**
     * Retorna el elemento de la cima sin eliminarlo.
     * (ANTES LLAMADO "elemento", aquí lo normalizamos como peek)
     * Complejidad: O(1)
     */
    public T elemento() {
        if (estaVacia()) {
            throw new RuntimeException("Pila vacía");
        }
        return lista.verPrimero();
    }

    // ─────────────────────────────────────────────
    // ESTADO (CORREGIDO)
    // ─────────────────────────────────────────────

    /**
     * Verifica si la pila está vacía.
     * Complejidad: O(1)
     */
    public boolean estaVacia() {
        return lista.estaVacia();
    }

    /**
     * Retorna el tamaño de la pila.
     * Complejidad: O(1)
     */
    public int tamanio() {
        return lista.tamanio();
    }

    /**
     * Limpia toda la pila.
     */
    public void limpiar() {
        lista.limpiar();
    }

    // ─────────────────────────────────────────────
    // REPRESENTACIÓN
    // ─────────────────────────────────────────────

    @Override
    public String toString() {
        return "Stack (top → bottom): " + lista.toString();
    }
}