package com.uniquindio.Estructuras;


import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Lista dinámica basada en arreglo con redimensionamiento automático.
 *
 * Usos en PropTech:
 *   - Catálogo principal de inmuebles
 *   - Lista de clientes registrados
 *   - Lista de asesores
 *   - Resultados de búsqueda y filtrado
 *   - Recomendaciones generadas para un cliente
 *   - Ranking de zonas y asesores
 *   - Visitas del día de un asesor
 */
public class ArrayListPropio<T> implements Iterable<T> {

    private static final int CAPACIDAD_INICIAL = 10;

    private Object[] datos;
    private int tamanio;

    // ─────────────────────────────────────────────
    // Constructores
    // ─────────────────────────────────────────────

    public ArrayListPropio() {
        this.datos = new Object[CAPACIDAD_INICIAL];
        this.tamanio = 0;
    }

    public ArrayListPropio(int capacidadInicial) {
        this.datos = new Object[capacidadInicial];
        this.tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // Redimensionamiento interno
    // ─────────────────────────────────────────────

    private void redimensionar() {

        Object[] nuevoArreglo = new Object[datos.length * 2];

        for (int i = 0; i < tamanio; i++) {
            nuevoArreglo[i] = datos[i];
        }

        datos = nuevoArreglo;
    }

    private void verificarCapacidad() {

        if (tamanio == datos.length) {
            redimensionar();
        }
    }

    private void validarIndice(int indice) {

        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
            );
        }
    }

    // ─────────────────────────────────────────────
    // Métodos de inserción
    // ─────────────────────────────────────────────

    /**
     * Agrega un elemento al final.
     * Complejidad: O(1) amortizado
     */
    public void agregar(T dato) {

        verificarCapacidad();

        datos[tamanio] = dato;

        tamanio++;
    }

    /**
     * Inserta un elemento en una posición específica.
     * Complejidad: O(n)
     */
    public void agregar(int indice, T dato) {

        if (indice < 0 || indice > tamanio) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + indice
            );
        }

        verificarCapacidad();

        for (int i = tamanio; i > indice; i--) {
            datos[i] = datos[i - 1];
        }

        datos[indice] = dato;

        tamanio++;
    }

    // ─────────────────────────────────────────────
    // Métodos de actualización
    // ─────────────────────────────────────────────

    /**
     * Reemplaza el elemento en la posición indicada.
     * Complejidad: O(1)
     */
    public void actualizar(int indice, T dato) {

        validarIndice(indice);

        datos[indice] = dato;
    }

    // ─────────────────────────────────────────────
    // Métodos de eliminación
    // ─────────────────────────────────────────────

    /**
     * Elimina el elemento en la posición indicada.
     * Complejidad: O(n)
     */
    @SuppressWarnings("unchecked")
    public T eliminar(int indice) {

        validarIndice(indice);

        T dato = (T) datos[indice];

        for (int i = indice; i < tamanio - 1; i++) {
            datos[i] = datos[i + 1];
        }

        datos[tamanio - 1] = null;

        tamanio--;

        return dato;
    }

    /**
     * Elimina la primera ocurrencia del dato.
     * Complejidad: O(n)
     */
    public boolean eliminar(T dato) {

        int indice = indiceDe(dato);

        if (indice == -1) {
            return false;
        }

        eliminar(indice);

        return true;
    }

    /**
     * Elimina todos los elementos que cumplan la condición.
     * Complejidad: O(n)
     */
    public int eliminarSi(Predicate<T> condicion) {

        int eliminados = 0;

        for (int i = tamanio - 1; i >= 0; i--) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            if (condicion.test(dato)) {

                eliminar(i);

                eliminados++;
            }
        }

        return eliminados;
    }

    // ─────────────────────────────────────────────
    // Métodos de consulta
    // ─────────────────────────────────────────────

    /**
     * Retorna el elemento en la posición indicada.
     * Complejidad: O(1)
     */
    @SuppressWarnings("unchecked")
    public T obtener(int indice) {

        validarIndice(indice);

        return (T) datos[indice];
    }

    /**
     * Retorna el primer elemento.
     */
    @SuppressWarnings("unchecked")
    public T verPrimero() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        return (T) datos[0];
    }

    /**
     * Retorna el último elemento.
     */
    @SuppressWarnings("unchecked")
    public T verUltimo() {

        if (estaVacia()) {
            throw new RuntimeException("La lista está vacía");
        }

        return (T) datos[tamanio - 1];
    }

    /**
     * Busca el primer elemento que cumpla la condición.
     * Complejidad: O(n)
     */
    public T buscar(Predicate<T> condicion) {

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            if (condicion.test(dato)) {
                return dato;
            }
        }

        return null;
    }

    /**
     * Busca todos los elementos que cumplan la condición.
     * Complejidad: O(n)
     */
    public ArrayListPropio<T> buscarTodos(Predicate<T> condicion) {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            if (condicion.test(dato)) {
                resultado.agregar(dato);
            }
        }

        return resultado;
    }

    /**
     * Alias semántico de buscarTodos.
     */
    public ArrayListPropio<T> filtrar(Predicate<T> condicion) {
        return buscarTodos(condicion);
    }

    /**
     * Verifica si existe un dato.
     * Complejidad: O(n)
     */
    public boolean contiene(T dato) {
        return indiceDe(dato) != -1;
    }

    /**
     * Retorna el índice de la primera ocurrencia.
     * Complejidad: O(n)
     */
    public int indiceDe(T dato) {

        for (int i = 0; i < tamanio; i++) {

            if (datos[i].equals(dato)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Cuenta elementos que cumplen una condición.
     * Complejidad: O(n)
     */
    public int contar(Predicate<T> condicion) {

        int cont = 0;

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            if (condicion.test(dato)) {
                cont++;
            }
        }

        return cont;
    }

    /**
     * Retorna el elemento mínimo según comparador.
     */
    public T minimo(Comparator<T> comparador) {

        if (estaVacia()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T min = (T) datos[0];

        for (int i = 1; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T actual = (T) datos[i];

            if (comparador.compare(actual, min) < 0) {
                min = actual;
            }
        }

        return min;
    }

    /**
     * Retorna el elemento máximo según comparador.
     */
    public T maximo(Comparator<T> comparador) {

        if (estaVacia()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T max = (T) datos[0];

        for (int i = 1; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T actual = (T) datos[i];

            if (comparador.compare(actual, max) > 0) {
                max = actual;
            }
        }

        return max;
    }

    // ─────────────────────────────────────────────
    // Ordenamiento (QuickSort)
    // ─────────────────────────────────────────────

    /**
     * Ordena la lista usando QuickSort.
     * Complejidad promedio: O(n log n)
     */
    public void ordenar(Comparator<T> comparador) {

        quickSort(0, tamanio - 1, comparador);
    }

    private void quickSort(
            int bajo,
            int alto,
            Comparator<T> comparador
    ) {

        if (bajo < alto) {

            int pivote = particion(
                    bajo,
                    alto,
                    comparador
            );

            quickSort(
                    bajo,
                    pivote - 1,
                    comparador
            );

            quickSort(
                    pivote + 1,
                    alto,
                    comparador
            );
        }
    }

    @SuppressWarnings("unchecked")
    private int particion(
            int bajo,
            int alto,
            Comparator<T> comparador
    ) {

        T pivote = (T) datos[alto];

        int i = bajo - 1;

        for (int j = bajo; j < alto; j++) {

            T actual = (T) datos[j];

            if (comparador.compare(actual, pivote) <= 0) {

                i++;

                intercambiar(i, j);
            }
        }

        intercambiar(i + 1, alto);

        return i + 1;
    }

    private void intercambiar(int i, int j) {

        Object temp = datos[i];

        datos[i] = datos[j];

        datos[j] = temp;
    }

    /**
     * Retorna una copia ordenada.
     */
    public ArrayListPropio<T> ordenarCopia(
            Comparator<T> comparador
    ) {

        ArrayListPropio<T> copia = copia();
        copia.ordenar(comparador);

        return copia;
    }

    // ─────────────────────────────────────────────
    // Métodos adicionales
    // ─────────────────────────────────────────────

    /**
     * Retorna una copia de la lista.
     */
    public ArrayListPropio<T> copia() {

        ArrayListPropio<T> copia =
                new ArrayListPropio<>(tamanio);

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            copia.agregar(dato);
        }

        return copia;
    }

    /**
     * Ejecuta una acción sobre cada elemento.
     */
    @Override
    public void forEach(Consumer<? super T> accion){

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            accion.accept(dato);
        }
    }

    /**
     * Invierte el orden de la lista.
     */
    public void invertir() {

        int izquierda = 0;
        int derecha = tamanio - 1;

        while (izquierda < derecha) {

            intercambiar(izquierda, derecha);

            izquierda++;
            derecha--;
        }
    }

    // ─────────────────────────────────────────────
    // Métodos de estado
    // ─────────────────────────────────────────────

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {

        datos = new Object[CAPACIDAD_INICIAL];

        tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // Conversión
    // ─────────────────────────────────────────────

    /**
     * Convierte a LinkedList propia.
     */
    public LinkedListPropia<T> aLinkedList() {

        LinkedListPropia<T> lista =
                new LinkedListPropia<>();

        for (int i = 0; i < tamanio; i++) {

            @SuppressWarnings("unchecked")
            T dato = (T) datos[i];

            lista.agregarAlFinal(dato);
        }

        return lista;
    }

    /**
     * Convierte a arreglo simple.
     */
    public Object[] aArreglo() {

        Object[] copia = new Object[tamanio];

        for (int i = 0; i < tamanio; i++) {
            copia[i] = datos[i];
        }

        return copia;
    }

    // ─────────────────────────────────────────────
    // Iterable
    // ─────────────────────────────────────────────

    @Override
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }

    /**
     * Iterador interno del ArrayList.
     */
    private class ArrayListIterator
            implements Iterator<T> {

        private int indiceActual = 0;

        @Override
        public boolean hasNext() {
            return indiceActual < tamanio;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {

            if (!hasNext()) {

                throw new NoSuchElementException(
                        "No hay más elementos"
                );
            }

            return (T) datos[indiceActual++];
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

        StringBuilder sb =
                new StringBuilder("[ ");

        for (int i = 0; i < tamanio; i++) {

            sb.append(datos[i]);

            if (i < tamanio - 1) {
                sb.append(", ");
            }
        }

        sb.append(" ]");

        return sb.toString();
    }
}