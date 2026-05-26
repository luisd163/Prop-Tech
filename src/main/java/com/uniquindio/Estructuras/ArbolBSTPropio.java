package com.uniquindio.Estructuras;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Árbol Binario de Búsqueda (BST) genérico.
 *
 * Requiere un Comparator para mantener el orden.
 *
 * Usos en PropTech:
 * - Buscar inmuebles por precio
 * - Consultas por rangos
 * - Rankings
 * - Ordenamiento automático
 * - Búsquedas eficientes
 */
public class ArbolBSTPropio<T> {

    // ═════════════════════════════════════════════
    // Nodo interno del árbol
    // ═════════════════════════════════════════════

    private static class NodoArbol<T> {

        T dato;
        NodoArbol<T> izquierdo;
        NodoArbol<T> derecho;

        NodoArbol(T dato) {
            this.dato = dato;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    // ═════════════════════════════════════════════
    // Atributos
    // ═════════════════════════════════════════════

    private NodoArbol<T> raiz;
    private final Comparator<T> comparador;
    private int tamanio;

    // ═════════════════════════════════════════════
    // Constructor
    // ═════════════════════════════════════════════

    public ArbolBSTPropio(Comparator<T> comparador) {

        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser null");
        }

        this.raiz = null;
        this.comparador = comparador;
        this.tamanio = 0;
    }

    // ═════════════════════════════════════════════
    // Inserción
    // ═════════════════════════════════════════════

    /**
     * Inserta un elemento manteniendo el orden BST.
     *
     * Complejidad:
     * O(log n) promedio
     * O(n) peor caso
     */
    public boolean insertar(T dato) {

        if (dato == null) {
            throw new IllegalArgumentException("No se puede insertar null");
        }

        int tamAnterior = tamanio;

        raiz = insertarRec(raiz, dato);

        return tamanio > tamAnterior;
    }

    private NodoArbol<T> insertarRec(NodoArbol<T> nodo, T dato) {

        if (nodo == null) {
            tamanio++;
            return new NodoArbol<>(dato);
        }

        int cmp = comparador.compare(dato, nodo.dato);

        if (cmp < 0) {

            nodo.izquierdo = insertarRec(nodo.izquierdo, dato);

        } else if (cmp > 0) {

            nodo.derecho = insertarRec(nodo.derecho, dato);

        } else {

            // Actualizar dato duplicado
            nodo.dato = dato;
        }

        return nodo;
    }

    // ═════════════════════════════════════════════
    // Búsqueda
    // ═════════════════════════════════════════════

    public T buscar(T dato) {

        NodoArbol<T> resultado = buscarNodo(raiz, dato);

        return resultado != null ? resultado.dato : null;
    }

    private NodoArbol<T> buscarNodo(NodoArbol<T> nodo, T dato) {

        if (nodo == null) return null;

        int cmp = comparador.compare(dato, nodo.dato);

        if (cmp == 0) return nodo;

        if (cmp < 0) {
            return buscarNodo(nodo.izquierdo, dato);
        }

        return buscarNodo(nodo.derecho, dato);
    }

    public boolean contiene(T dato) {
        return buscar(dato) != null;
    }

    // ═════════════════════════════════════════════
    // Búsqueda por rango
    // ═════════════════════════════════════════════

    public ArrayListPropio<T> buscarRango(T min, T max) {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        buscarRangoRec(raiz, min, max, resultado);

        return resultado;
    }

    private void buscarRangoRec(
            NodoArbol<T> nodo,
            T min,
            T max,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null) return;

        int cmpMin = comparador.compare(nodo.dato, min);
        int cmpMax = comparador.compare(nodo.dato, max);

        if (cmpMin > 0) {
            buscarRangoRec(nodo.izquierdo, min, max, resultado);
        }

        if (cmpMin >= 0 && cmpMax <= 0) {
            resultado.agregar(nodo.dato);
        }

        if (cmpMax < 0) {
            buscarRangoRec(nodo.derecho, min, max, resultado);
        }
    }

    // ═════════════════════════════════════════════
    // Búsqueda con filtro
    // ═════════════════════════════════════════════

    public ArrayListPropio<T> buscarConFiltro(Predicate<T> filtro) {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        buscarConFiltroRec(raiz, filtro, resultado);

        return resultado;
    }

    private void buscarConFiltroRec(
            NodoArbol<T> nodo,
            Predicate<T> filtro,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null) return;

        buscarConFiltroRec(nodo.izquierdo, filtro, resultado);

        if (filtro.test(nodo.dato)) {
            resultado.agregar(nodo.dato);
        }

        buscarConFiltroRec(nodo.derecho, filtro, resultado);
    }

    // ═════════════════════════════════════════════
    // Mínimo y máximo
    // ═════════════════════════════════════════════

    public T minimo() {

        if (estaVacio()) {
            throw new RuntimeException("Árbol vacío");
        }

        return minimoNodo(raiz).dato;
    }

    private NodoArbol<T> minimoNodo(NodoArbol<T> nodo) {

        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }

        return nodo;
    }

    public T maximo() {

        if (estaVacio()) {
            throw new RuntimeException("Árbol vacío");
        }

        NodoArbol<T> actual = raiz;

        while (actual.derecho != null) {
            actual = actual.derecho;
        }

        return actual.dato;
    }

    // ═════════════════════════════════════════════
    // Eliminación
    // ═════════════════════════════════════════════

    public boolean eliminar(T dato) {

        if (!contiene(dato)) {
            return false;
        }

        raiz = eliminarRec(raiz, dato);

        tamanio--;

        return true;
    }

    private NodoArbol<T> eliminarRec(NodoArbol<T> nodo, T dato) {

        if (nodo == null) return null;

        int cmp = comparador.compare(dato, nodo.dato);

        if (cmp < 0) {

            nodo.izquierdo = eliminarRec(nodo.izquierdo, dato);

        } else if (cmp > 0) {

            nodo.derecho = eliminarRec(nodo.derecho, dato);

        } else {

            // Caso 1: hoja
            if (nodo.izquierdo == null && nodo.derecho == null) {
                return null;
            }

            // Caso 2: un hijo
            if (nodo.izquierdo == null) {
                return nodo.derecho;
            }

            if (nodo.derecho == null) {
                return nodo.izquierdo;
            }

            // Caso 3: dos hijos
            NodoArbol<T> sucesor = minimoNodo(nodo.derecho);

            nodo.dato = sucesor.dato;

            nodo.derecho = eliminarRec(nodo.derecho, sucesor.dato);
        }

        return nodo;
    }

    // ═════════════════════════════════════════════
    // Recorridos
    // ═════════════════════════════════════════════

    public ArrayListPropio<T> inorden() {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        inordenRec(raiz, resultado);

        return resultado;
    }

    private void inordenRec(
            NodoArbol<T> nodo,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null) return;

        inordenRec(nodo.izquierdo, resultado);

        resultado.agregar(nodo.dato);

        inordenRec(nodo.derecho, resultado);
    }

    public ArrayListPropio<T> preorden() {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        preordenRec(raiz, resultado);

        return resultado;
    }

    private void preordenRec(
            NodoArbol<T> nodo,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null) return;

        resultado.agregar(nodo.dato);

        preordenRec(nodo.izquierdo, resultado);

        preordenRec(nodo.derecho, resultado);
    }

    public ArrayListPropio<T> postorden() {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        postordenRec(raiz, resultado);

        return resultado;
    }

    private void postordenRec(
            NodoArbol<T> nodo,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null) return;

        postordenRec(nodo.izquierdo, resultado);

        postordenRec(nodo.derecho, resultado);

        resultado.agregar(nodo.dato);
    }

    /**
     * Recorrido BFS por niveles.
     */
    public ArrayListPropio<T> porNiveles() {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        if (raiz == null) return resultado;

        ColaPropia<NodoArbol<T>> cola = new ColaPropia<>();

        cola.encolar(raiz);

        while (!cola.estaVacia()) {

            NodoArbol<T> actual = cola.desencolar();

            resultado.agregar(actual.dato);

            if (actual.izquierdo != null) {
                cola.encolar(actual.izquierdo);
            }

            if (actual.derecho != null) {
                cola.encolar(actual.derecho);
            }
        }

        return resultado;
    }

    // ═════════════════════════════════════════════
    // ForEach
    // ═════════════════════════════════════════════

    /**
     * Recorre el árbol en inorden aplicando una acción.
     */
    public void forEach(Consumer<T> accion) {

        if (accion == null) {
            throw new IllegalArgumentException("La acción no puede ser null");
        }

        forEachRec(raiz, accion);
    }

    private void forEachRec(
            NodoArbol<T> nodo,
            Consumer<T> accion
    ) {

        if (nodo == null) return;

        forEachRec(nodo.izquierdo, accion);

        accion.accept(nodo.dato);

        forEachRec(nodo.derecho, accion);
    }

    // ═════════════════════════════════════════════
    // Conversión
    // ═════════════════════════════════════════════

    /**
     * Convierte el árbol a ArrayList ordenado.
     */
    public ArrayListPropio<T> aArrayList() {
        return inorden();
    }

    // ═════════════════════════════════════════════
    // Métricas
    // ═════════════════════════════════════════════

    public int altura() {
        return alturaRec(raiz);
    }

    private int alturaRec(NodoArbol<T> nodo) {

        if (nodo == null) return -1;

        return 1 + Math.max(
                alturaRec(nodo.izquierdo),
                alturaRec(nodo.derecho)
        );
    }

    public int contarHojas() {
        return contarHojasRec(raiz);
    }

    private int contarHojasRec(NodoArbol<T> nodo) {

        if (nodo == null) return 0;

        if (nodo.izquierdo == null && nodo.derecho == null) {
            return 1;
        }

        return contarHojasRec(nodo.izquierdo)
                + contarHojasRec(nodo.derecho);
    }

    public boolean estaBalanceado() {
        return verificarBalance(raiz) != -1;
    }

    private int verificarBalance(NodoArbol<T> nodo) {

        if (nodo == null) return 0;

        int izq = verificarBalance(nodo.izquierdo);
        int der = verificarBalance(nodo.derecho);

        if (izq == -1 || der == -1) {
            return -1;
        }

        if (Math.abs(izq - der) > 1) {
            return -1;
        }

        return 1 + Math.max(izq, der);
    }

    // ═════════════════════════════════════════════
    // K mayores y menores
    // ═════════════════════════════════════════════

    public ArrayListPropio<T> kMayores(int k) {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        kMayoresRec(raiz, k, resultado);

        return resultado;
    }

    private void kMayoresRec(
            NodoArbol<T> nodo,
            int k,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null || resultado.tamanio() >= k) {
            return;
        }

        kMayoresRec(nodo.derecho, k, resultado);

        if (resultado.tamanio() < k) {
            resultado.agregar(nodo.dato);
        }

        kMayoresRec(nodo.izquierdo, k, resultado);
    }

    public ArrayListPropio<T> kMenores(int k) {

        ArrayListPropio<T> resultado = new ArrayListPropio<>();

        kMenoresRec(raiz, k, resultado);

        return resultado;
    }

    private void kMenoresRec(
            NodoArbol<T> nodo,
            int k,
            ArrayListPropio<T> resultado
    ) {

        if (nodo == null || resultado.tamanio() >= k) {
            return;
        }

        kMenoresRec(nodo.izquierdo, k, resultado);

        if (resultado.tamanio() < k) {
            resultado.agregar(nodo.dato);
        }

        kMenoresRec(nodo.derecho, k, resultado);
    }

    // ═════════════════════════════════════════════
    // Estado
    // ═════════════════════════════════════════════

    public boolean estaVacio() {
        return raiz == null;
    }

    public int tamanio() {
        return tamanio;
    }

    public void limpiar() {
        raiz = null;
        tamanio = 0;
    }

    // ═════════════════════════════════════════════
    // String
    // ═════════════════════════════════════════════

    @Override
    public String toString() {
        return "ArbolBST " + inorden().toString();
    }
}