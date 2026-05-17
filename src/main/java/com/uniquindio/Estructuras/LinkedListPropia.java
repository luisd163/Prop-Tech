package com.uniquindio.Estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Lista enlazada simple genérica.
 *
 * Usos en PropTech:
 *   - Historial de visitas de un cliente
 *   - Lista de favoritos por cliente
 *   - Inmuebles asignados a un asesor
 *   - Contratos y operaciones registradas
 *   - Cola de alertas pendientes
 *   - Base para implementar Pila y Cola
 *   - Buckets de la TablaHash (manejo de colisiones)
 */
public class LinkedListPropia<T> implements Iterable<T> {

    private Nodo<T> cabeza;
    private int tamanio;

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    public LinkedListPropia() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // Métodos privados auxiliares
    // ─────────────────────────────────────────────

    /**
     * Retorna el nodo en la posición indicada.
     * Complejidad: O(n)
     */
    private Nodo<T> obtenerNodo(int indice) {
        Nodo<T> actual = cabeza;

        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }

        return actual;
    }

    // ─────────────────────────────────────────────
    // Métodos de inserción
    // ─────────────────────────────────────────────

    /**
     * Agrega un elemento al final de la lista.
     * Uso: agregar visita al historial, agregar favorito.
     * Complejidad: O(n)
     */
    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;

            while (actual.getSiguiente() != null) {
                actual = actual.siguiente;
            }

            actual.siguiente = nuevo;
        }

        tamanio++;
    }

    /**
     * Agrega un elemento al inicio de la lista.
     * Uso: push en Pila.
     * Complejidad: O(1)
     */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);

        nuevo.siguiente = cabeza;
        cabeza = nuevo;

        tamanio++;
    }

    /**
     * Inserta un elemento en una posición específica.
     * Complejidad: O(n)
     */
    public void insertarEn(int indice, T dato) {

        if (indice < 0 || indice > tamanio) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
            );
        }

        if (indice == 0) {
            agregarAlInicio(dato);
            return;
        }

        Nodo<T> anterior = obtenerNodo(indice - 1);

        Nodo<T> nuevo = new Nodo<>(dato);

        nuevo.siguiente = anterior.siguiente;
        anterior.siguiente = nuevo;

        tamanio++;
    }

    // ─────────────────────────────────────────────
    // Métodos de eliminación
    // ─────────────────────────────────────────────

    /**
     * Elimina y retorna el primer elemento.
     * Uso: Cola → dequeue | Pila → pop
     * Complejidad: O(1)
     */
    public T eliminarAlInicio() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        T dato = cabeza.getDato();

        cabeza = cabeza.siguiente;

        tamanio--;

        return dato;
    }

    /**
     * Elimina y retorna el último elemento.
     * Complejidad: O(n)
     */
    public T eliminarAlFinal() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        if (cabeza.siguiente == null) {

            T dato = cabeza.getDato();

            cabeza = null;

            tamanio--;

            return dato;
        }

        Nodo<T> actual = cabeza;

        while (actual.siguiente.siguiente != null) {
            actual = actual.siguiente;
        }

        T dato = actual.siguiente.getDato();

        actual.siguiente = null;

        tamanio--;

        return dato;
    }

    /**
     * Elimina la primera ocurrencia de un dato.
     * Complejidad: O(n)
     */
    public boolean eliminar(T dato) {

        if (estaVacia()) return false;

        if (cabeza.getDato().equals(dato)) {

            cabeza = cabeza.siguiente;

            tamanio--;

            return true;
        }

        Nodo<T> actual = cabeza;

        while (actual.siguiente != null) {

            if (actual.siguiente.getDato().equals(dato)) {

                actual.siguiente = actual.siguiente.siguiente;

                tamanio--;

                return true;
            }

            actual = actual.siguiente;
        }

        return false;
    }

    /**
     * Elimina el elemento en la posición indicada.
     * Complejidad: O(n)
     */
    public T eliminarEn(int indice) {

        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
            );
        }

        if (indice == 0) {
            return eliminarAlInicio();
        }

        Nodo<T> anterior = obtenerNodo(indice - 1);

        T dato = anterior.siguiente.getDato();

        anterior.siguiente = anterior.siguiente.siguiente;

        tamanio--;

        return dato;
    }

    /**
     * Elimina todos los elementos que cumplan una condición.
     * Complejidad: O(n)
     */
    public int eliminarSi(Predicate<T> condicion) {

        int eliminados = 0;

        while (cabeza != null && condicion.test(cabeza.getDato())) {

            cabeza = cabeza.siguiente;

            tamanio--;

            eliminados++;
        }

        if (cabeza == null) return eliminados;

        Nodo<T> actual = cabeza;

        while (actual.siguiente != null) {

            if (condicion.test(actual.siguiente.getDato())) {

                actual.siguiente = actual.siguiente.siguiente;

                tamanio--;

                eliminados++;

            } else {

                actual = actual.siguiente;
            }
        }

        return eliminados;
    }

    // ─────────────────────────────────────────────
    // Métodos de consulta
    // ─────────────────────────────────────────────

    /**
     * Retorna el elemento en la posición indicada.
     * Complejidad: O(n)
     */
    public T obtener(int indice) {

        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
            );
        }

        return obtenerNodo(indice).getDato();
    }

    /**
     * Retorna el primer elemento sin eliminarlo.
     * Complejidad: O(1)
     */
    public T verPrimero() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        return cabeza.getDato();
    }

    /**
     * Retorna el último elemento sin eliminarlo.
     * Complejidad: O(n)
     */
    public T verUltimo() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        Nodo<T> actual = cabeza;

        while (actual.siguiente != null) {
            actual = actual.siguiente;
        }

        return actual.getDato();
    }

    /**
     * Busca la primera ocurrencia que cumpla la condición.
     * Complejidad: O(n)
     */
    public T buscar(Predicate<T> condicion) {

        Nodo<T> actual = cabeza;

        while (actual != null) {

            if (condicion.test(actual.getDato())) {
                return actual.getDato();
            }

            actual = actual.siguiente;
        }

        return null;
    }

    /**
     * Verifica si un dato existe en la lista.
     * Complejidad: O(n)
     */
    public boolean contiene(T dato) {
        return buscar(d -> d.equals(dato)) != null;
    }

    /**
     * Retorna el índice de la primera ocurrencia.
     * Complejidad: O(n)
     */
    public int indiceDe(T dato) {

        Nodo<T> actual = cabeza;

        int indice = 0;

        while (actual != null) {

            if (actual.getDato().equals(dato)) {
                return indice;
            }

            actual = actual.siguiente;

            indice++;
        }

        return -1;
    }

    /**
     * Retorna una nueva lista con los elementos filtrados.
     * Complejidad: O(n)
     */
    public LinkedListPropia<T> filtrar(Predicate<T> condicion) {

        LinkedListPropia<T> resultado = new LinkedListPropia<>();

        Nodo<T> actual = cabeza;

        while (actual != null) {

            if (condicion.test(actual.getDato())) {
                resultado.agregarAlFinal(actual.getDato());
            }

            actual = actual.siguiente;
        }

        return resultado;
    }

    /**
     * Cuenta elementos que cumplen una condición.
     * Complejidad: O(n)
     */
    public int contar(Predicate<T> condicion) {

        int cont = 0;

        Nodo<T> actual = cabeza;

        while (actual != null) {

            if (condicion.test(actual.getDato())) {
                cont++;
            }

            actual = actual.siguiente;
        }

        return cont;
    }

    // ─────────────────────────────────────────────
    // Métodos adicionales
    // ─────────────────────────────────────────────

    /**
     * Ejecuta una acción sobre cada elemento.
     * Complejidad: O(n)
     */
    @Override
    public void forEach(Consumer<? super T> accion) {

        Nodo<T> actual = cabeza;

        while (actual != null) {

            accion.accept(actual.getDato());

            actual = actual.siguiente;
        }
    }

    /**
     * Retorna una nueva lista invertida.
     * Complejidad: O(n)
     */
    public LinkedListPropia<T> reversa() {

        LinkedListPropia<T> invertida = new LinkedListPropia<>();

        Nodo<T> actual = cabeza;

        while (actual != null) {

            invertida.agregarAlInicio(actual.getDato());

            actual = actual.siguiente;
        }

        return invertida;
    }

    // ─────────────────────────────────────────────
    // Métodos de estado
    // ─────────────────────────────────────────────

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public void limpiar() {
        cabeza = null;
        tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // Conversión
    // ─────────────────────────────────────────────

    /**
     * Convierte la lista a arreglo.
     */
    public Object[] aArreglo() {

        Object[] arr = new Object[tamanio];

        Nodo<T> actual = cabeza;

        for (int i = 0; i < tamanio; i++) {

            arr[i] = actual.getDato();

            actual = actual.siguiente;
        }

        return arr;
    }

    /**
     * Convierte a ArrayList propio.
     */
    public ArrayListPropio<T> aArrayList() {

        ArrayListPropio<T> lista = new ArrayListPropio<>();

        Nodo<T> actual = cabeza;

        while (actual != null) {

            lista.agregar(actual.getDato());

            actual = actual.siguiente;
        }

        return lista;
    }

    // ─────────────────────────────────────────────
    // Iterable
    // ─────────────────────────────────────────────

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /**
     * Iterador interno de la lista enlazada.
     */
    private class LinkedListIterator implements Iterator<T> {

        private Nodo<T> actual = cabeza;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {

            if (!hasNext()) {
                throw new NoSuchElementException(
                        "No hay más elementos"
                );
            }

            T dato = actual.getDato();

            actual = actual.siguiente;

            return dato;
        }
    }

    // ─────────────────────────────────────────────
    // Visualización
    // ─────────────────────────────────────────────

    @Override
    public String toString() {

        if (estaVacia()) {
            return "[ vacía ]";
        }

        StringBuilder sb = new StringBuilder("[ ");

        Nodo<T> actual = cabeza;

        while (actual != null) {

            sb.append(actual.getDato());

            if (actual.siguiente != null) {
                sb.append(" → ");
            }

            actual = actual.siguiente;
        }

        sb.append(" ]");

        return sb.toString();
    }
}