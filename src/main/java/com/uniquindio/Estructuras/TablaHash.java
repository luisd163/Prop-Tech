package com.uniquindio.Estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Tabla Hash genérica con encadenamiento (separate chaining).
 *
 * Usos en PropTech:
 *   - Clientes por ID (O(1) promedio)
 *   - Inmuebles por código
 *   - Asesores por identificación
 *   - Conteo de visitas
 *   - Agrupaciones por ciudad o zona
 */
public class TablaHash<K, V> implements Iterable<TablaHash.Entrada<K, V>> {

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA_MAX = 0.75;

    private LinkedListPropia<Entrada<K, V>>[] buckets;
    private int tamanio;
    private int capacidad;

    // ─────────────────────────────────────────────
    // Entrada clave-valor
    // ─────────────────────────────────────────────

    public static class Entrada<K, V> {
        public K clave;
        public V valor;

        public Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }

        @Override
        public String toString() {
            return clave + " → " + valor;
        }
    }

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public TablaHash() {
        this.capacidad = CAPACIDAD_INICIAL;
        this.buckets = new LinkedListPropia[capacidad];
        this.tamanio = 0;
        inicializarBuckets();
    }

    private void inicializarBuckets() {
        for (int i = 0; i < capacidad; i++) {
            buckets[i] = new LinkedListPropia<>();
        }
    } 

    // ─────────────────────────────────────────────
    // Hash
    // ─────────────────────────────────────────────

    private int hash(K clave) {
        return Math.abs(clave.hashCode() % capacidad);
    }

    // ─────────────────────────────────────────────
    // Rehash
    // ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void rehash() {

        int nuevaCapacidad = capacidad * 2;
        LinkedListPropia<Entrada<K, V>>[] nuevo = new LinkedListPropia[nuevaCapacidad];

        for (int i = 0; i < nuevaCapacidad; i++) {
            nuevo[i] = new LinkedListPropia<>();
        }

        for (Entrada<K, V> e : this) {
            int idx = Math.abs(e.clave.hashCode() % nuevaCapacidad);
            nuevo[idx].agregarAlFinal(e);
        }

        this.buckets = nuevo;
        this.capacidad = nuevaCapacidad;
    }

    // ─────────────────────────────────────────────
    // Insertar / actualizar
    // ─────────────────────────────────────────────

    public void insertar(K clave, V valor) {

        if ((double) tamanio / capacidad >= FACTOR_CARGA_MAX) {
            rehash();
        }

        int idx = hash(clave);

        LinkedListPropia<Entrada<K, V>> bucket = buckets[idx];

        for (int i = 0; i < bucket.tamanio(); i++) {

            Entrada<K, V> e = bucket.obtener(i);

            if (e.clave.equals(clave)) {
                e.valor = valor;
                return;
            }
        }

        bucket.agregarAlFinal(new Entrada<>(clave, valor));
        tamanio++;
    }

    // ─────────────────────────────────────────────
    // Buscar
    // ─────────────────────────────────────────────

    public V buscar(K clave) {

        int idx = hash(clave);

        LinkedListPropia<Entrada<K, V>> bucket = buckets[idx];

        for (int i = 0; i < bucket.tamanio(); i++) {

            Entrada<K, V> e = bucket.obtener(i);

            if (e.clave.equals(clave)) {
                return e.valor;
            }
        }

        return null;
    }

    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    // ─────────────────────────────────────────────
    // Eliminar
    // ─────────────────────────────────────────────

    public boolean eliminar(K clave) {

        int idx = hash(clave);

        LinkedListPropia<Entrada<K, V>> bucket = buckets[idx];

        for (int i = 0; i < bucket.tamanio(); i++) {

            if (bucket.obtener(i).clave.equals(clave)) {

                bucket.eliminarEn(i);

                tamanio--;

                return true;
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────
    // Agrupación
    // ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public void agrupar(K clave, V valor) {

        V actual = buscar(clave);

        LinkedListPropia<V> lista;

        if (actual == null) {
            lista = new LinkedListPropia<>();
        } else {
            lista = (LinkedListPropia<V>) actual;
        }

        lista.agregarAlFinal(valor);

        insertar(clave, (V) lista);
    }

    // ─────────────────────────────────────────────
    // Vistas globales
    // ─────────────────────────────────────────────

    public ArrayListPropio<V> valores() {

        ArrayListPropio<V> lista = new ArrayListPropio<>();

        for (Entrada<K, V> e : this) {
            lista.agregar(e.valor);
        }

        return lista;
    }

    public ArrayListPropio<K> claves() {

        ArrayListPropio<K> lista = new ArrayListPropio<>();

        for (Entrada<K, V> e : this) {
            lista.agregar(e.clave);
        }

        return lista;
    }

    public ArrayListPropio<Entrada<K, V>> entradas() {

        ArrayListPropio<Entrada<K, V>> lista = new ArrayListPropio<>();

        for (Entrada<K, V> e : this) {
            lista.agregar(e);
        }

        return lista;
    }

    // ─────────────────────────────────────────────
    // Estado
    // ─────────────────────────────────────────────

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public double factorCarga() {
        return (double) tamanio / capacidad;
    }

    public void limpiar() {

        inicializarBuckets();

        tamanio = 0;
    }

    // ─────────────────────────────────────────────
    // forEach
    // ─────────────────────────────────────────────

    @Override
    public void forEach(Consumer<? super Entrada<K, V>> accion) {

        for (Entrada<K, V> e : this) {
            accion.accept(e);
        }
    }

    // ─────────────────────────────────────────────
    // ITERADOR GLOBAL
    // ─────────────────────────────────────────────

    @Override
    public Iterator<Entrada<K, V>> iterator() {
        return new HashIterator();
    }

    private class HashIterator implements Iterator<Entrada<K, V>> {

        private int bucketIndex = 0;
        private int listIndex = 0;

        @Override
        public boolean hasNext() {

            while (bucketIndex < capacidad) {

                if (listIndex < buckets[bucketIndex].tamanio()) {
                    return true;
                }

                bucketIndex++;
                listIndex = 0;
            }

            return false;
        }

        @Override
        public Entrada<K, V> next() {

            if (!hasNext()) {
                throw new NoSuchElementException("Fin de la tabla hash");
            }

            return buckets[bucketIndex].obtener(listIndex++);
        }
    }

    // ─────────────────────────────────────────────
    // Visualización
    // ─────────────────────────────────────────────

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("TablaHash {\n");

        for (int i = 0; i < capacidad; i++) {

            if (!buckets[i].estaVacia()) {

                sb.append("  [")
                  .append(i)
                  .append("]: ")
                  .append(buckets[i])
                  .append("\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }
}