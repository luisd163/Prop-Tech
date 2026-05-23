package com.uniquindio.Estructuras;

import java.util.*;

public class Grafo<T> {

    // ─── Nodo interno ─────────────────────────────────────────────────────────
    private static class Nodo<T> {
        T valor;
        List<Nodo<T>> vecinos;

        Nodo(T valor) {
            this.valor   = valor;
            this.vecinos = new ArrayList<>();
        }
    }

    // ─── Estructura principal: mapa de nodos ──────────────────────────────────
    private final Map<T, Nodo<T>> nodos;
    private final boolean dirigido;

    // ─── Constructores ────────────────────────────────────────────────────────

    public Grafo() {
        this(false); // no dirigido por defecto
    }

    public Grafo(boolean dirigido) {
        this.nodos    = new LinkedHashMap<>();
        this.dirigido = dirigido;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. OPERACIONES BÁSICAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Agrega un nodo al grafo si no existe aún.
     */
    public void agregarNodo(T valor) {
        if (valor == null) throw new IllegalArgumentException("El valor del nodo no puede ser null");
        nodos.putIfAbsent(valor, new Nodo<>(valor));
    }

    /**
     * Elimina un nodo y todas sus aristas del grafo.
     */
    public void eliminarNodo(T valor) {
        if (!nodos.containsKey(valor)) return;
        nodos.remove(valor);
        // eliminar todas las aristas que apuntaban a este nodo
        for (Nodo<T> nodo : nodos.values()) {
            nodo.vecinos.removeIf(v -> v.valor.equals(valor));
        }
    }

    /**
     * Agrega una arista entre origen y destino.
     * Si el grafo no es dirigido, agrega la arista en ambos sentidos.
     * Si los nodos no existen los crea automáticamente.
     */
    public void agregarArista(T origen, T destino) {
        agregarNodo(origen);
        agregarNodo(destino);

        Nodo<T> nodoOrigen  = nodos.get(origen);
        Nodo<T> nodoDestino = nodos.get(destino);

        if (!tieneArista(origen, destino)) {
            nodoOrigen.vecinos.add(nodoDestino);
        }

        if (!dirigido && !tieneArista(destino, origen)) {
            nodoDestino.vecinos.add(nodoOrigen);
        }
    }

    /**
     * Elimina la arista entre origen y destino.
     */
    public void eliminarArista(T origen, T destino) {
        Nodo<T> nodoOrigen  = nodos.get(origen);
        Nodo<T> nodoDestino = nodos.get(destino);
        if (nodoOrigen == null || nodoDestino == null) return;

        nodoOrigen.vecinos.removeIf(v -> v.valor.equals(destino));
        if (!dirigido) {
            nodoDestino.vecinos.removeIf(v -> v.valor.equals(origen));
        }
    }

    /**
     * Verifica si existe una arista entre dos nodos.
     */
    public boolean tieneArista(T origen, T destino) {
        Nodo<T> nodoOrigen = nodos.get(origen);
        if (nodoOrigen == null) return false;
        return nodoOrigen.vecinos.stream().anyMatch(v -> v.valor.equals(destino));
    }

    /**
     * Verifica si un nodo existe en el grafo.
     */
    public boolean existeNodo(T valor) {
        return nodos.containsKey(valor);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. CONSULTAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve la lista de vecinos de un nodo.
     */
    public List<T> obtenerVecinos(T valor) {
        Nodo<T> nodo = nodos.get(valor);
        if (nodo == null) return Collections.emptyList();
        List<T> resultado = new ArrayList<>();
        nodo.vecinos.forEach(v -> resultado.add(v.valor));
        return resultado;
    }

    /**
     * Devuelve todos los nodos del grafo.
     */
    public List<T> obtenerNodos() {
        return new ArrayList<>(nodos.keySet());
    }

    /**
     * Devuelve el número de nodos del grafo.
     */
    public int cantidadNodos() {
        return nodos.size();
    }

    /**
     * Devuelve el grado de un nodo (cantidad de vecinos).
     */
    public int gradoNodo(T valor) {
        Nodo<T> nodo = nodos.get(valor);
        if (nodo == null) return 0;
        return nodo.vecinos.size();
    }

    /**
     * Devuelve el número total de aristas del grafo.
     */
    public int cantidadAristas() {
        int total = 0;
        for (Nodo<T> nodo : nodos.values()) {
            total += nodo.vecinos.size();
        }
        return dirigido ? total : total / 2;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. BFS — Búsqueda por anchura
    // Sirve para: encontrar el camino más corto, explorar zonas cercanas,
    // detectar todos los nodos alcanzables desde uno dado.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recorrido BFS desde un nodo origen.
     * Devuelve los nodos en orden de visita.
     */
    public List<T> bfs(T origen) {
        if (!nodos.containsKey(origen)) return Collections.emptyList();

        List<T>    visitados = new ArrayList<>();
        Set<T>     visto     = new LinkedHashSet<>();
        Queue<T>   cola      = new LinkedList<>();

        cola.add(origen);
        visto.add(origen);

        while (!cola.isEmpty()) {
            T actual = cola.poll();
            visitados.add(actual);

            for (T vecino : obtenerVecinos(actual)) {
                if (!visto.contains(vecino)) {
                    visto.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return visitados;
    }

    /**
     * Camino más corto entre dos nodos (en número de aristas).
     * Devuelve la lista de nodos del camino, o vacío si no existe.
     */
    public List<T> caminoMasCorto(T origen, T destino) {
        if (!nodos.containsKey(origen) || !nodos.containsKey(destino)) {
            return Collections.emptyList();
        }

        Map<T, T> padre  = new LinkedHashMap<>();
        Set<T>    visto  = new LinkedHashSet<>();
        Queue<T>  cola   = new LinkedList<>();

        cola.add(origen);
        visto.add(origen);
        padre.put(origen, null);

        while (!cola.isEmpty()) {
            T actual = cola.poll();
            if (actual.equals(destino)) break;

            for (T vecino : obtenerVecinos(actual)) {
                if (!visto.contains(vecino)) {
                    visto.add(vecino);
                    padre.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        if (!padre.containsKey(destino)) return Collections.emptyList();

        // Reconstruir el camino desde destino hasta origen
        LinkedList<T> camino = new LinkedList<>();
        T actual = destino;
        while (actual != null) {
            camino.addFirst(actual);
            actual = padre.get(actual);
        }
        return camino;
    }

    /**
     * Distancia (en aristas) entre dos nodos.
     * Devuelve -1 si no hay camino.
     */
    public int distancia(T origen, T destino) {
        List<T> camino = caminoMasCorto(origen, destino);
        return camino.isEmpty() ? -1 : camino.size() - 1;
    }

    /**
     * Devuelve todos los nodos alcanzables desde origen
     * dentro de un número máximo de saltos.
     * Útil para el módulo de locaciones cercanas.
     */
    public List<T> nodosDentroDeRadio(T origen, int maxSaltos) {
        if (!nodos.containsKey(origen)) return Collections.emptyList();

        Map<T, Integer> distancias = new LinkedHashMap<>();
        Queue<T>        cola       = new LinkedList<>();

        distancias.put(origen, 0);
        cola.add(origen);

        while (!cola.isEmpty()) {
            T actual  = cola.poll();
            int nivel = distancias.get(actual);
            if (nivel >= maxSaltos) continue;

            for (T vecino : obtenerVecinos(actual)) {
                if (!distancias.containsKey(vecino)) {
                    distancias.put(vecino, nivel + 1);
                    cola.add(vecino);
                }
            }
        }

        distancias.remove(origen); // excluir el nodo de origen
        return new ArrayList<>(distancias.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. DFS — Búsqueda por profundidad
    // Sirve para: detectar ciclos, componentes conexas,
    // análisis de patrones de comportamiento.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Recorrido DFS iterativo desde un nodo origen.
     * Devuelve los nodos en orden de visita.
     */
    public List<T> dfs(T origen) {
        if (!nodos.containsKey(origen)) return Collections.emptyList();

        List<T>   visitados = new ArrayList<>();
        Set<T>    visto     = new LinkedHashSet<>();
        Deque<T>  pila      = new ArrayDeque<>();

        pila.push(origen);

        while (!pila.isEmpty()) {
            T actual = pila.pop();
            if (visto.contains(actual)) continue;
            visto.add(actual);
            visitados.add(actual);

            List<T> vecinos = obtenerVecinos(actual);
            // Invertir para mantener orden natural
            for (int i = vecinos.size() - 1; i >= 0; i--) {
                if (!visto.contains(vecinos.get(i))) {
                    pila.push(vecinos.get(i));
                }
            }
        }
        return visitados;
    }

    /**
     * Verifica si el grafo tiene algún ciclo.
     * Útil para detectar dependencias circulares en operaciones.
     */
    public boolean tieneCiclo() {
        Set<T> visitados    = new HashSet<>();
        Set<T> enProceso    = new HashSet<>();

        for (T nodo : nodos.keySet()) {
            if (!visitados.contains(nodo)) {
                if (tieneCicloDesde(nodo, visitados, enProceso)) return true;
            }
        }
        return false;
    }

    private boolean tieneCicloDesde(T nodo, Set<T> visitados, Set<T> enProceso) {
        visitados.add(nodo);
        enProceso.add(nodo);

        for (T vecino : obtenerVecinos(nodo)) {
            if (!visitados.contains(vecino)) {
                if (tieneCicloDesde(vecino, visitados, enProceso)) return true;
            } else if (enProceso.contains(vecino)) {
                return true;
            }
        }
        enProceso.remove(nodo);
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. ANÁLISIS DE COMPONENTES CONEXAS
    // Sirve para detectar grupos de clientes o inmuebles sin conexión entre sí.
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve todas las componentes conexas del grafo.
     * Cada componente es una lista de nodos conectados entre sí.
     */
    public List<List<T>> componentesConexas() {
        Set<T>       visitados   = new HashSet<>();
        List<List<T>> componentes = new ArrayList<>();

        for (T nodo : nodos.keySet()) {
            if (!visitados.contains(nodo)) {
                List<T> componente = bfs(nodo);
                componentes.add(componente);
                visitados.addAll(componente);
            }
        }
        return componentes;
    }

    /**
     * Verifica si el grafo es conexo (todos los nodos están conectados).
     */
    public boolean esConexo() {
        if (nodos.isEmpty()) return true;
        T primero    = nodos.keySet().iterator().next();
        List<T> bfs  = bfs(primero);
        return bfs.size() == nodos.size();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 6. UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Limpia el grafo por completo.
     */
    public void limpiar() {
        nodos.clear();
    }

    /**
     * Devuelve los nodos ordenados por grado descendente.
     * Útil para ranking de inmuebles más visitados o zonas más activas.
     */
    public List<T> nodosPorGradoDesc() {
        List<T> lista = new ArrayList<>(nodos.keySet());
        lista.sort((a, b) -> gradoNodo(b) - gradoNodo(a));
        return lista;
    }

    /**
     * Representación textual del grafo para depuración.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grafo [dirigido=").append(dirigido)
          .append(", nodos=").append(cantidadNodos())
          .append(", aristas=").append(cantidadAristas()).append("]\n");

        for (Map.Entry<T, Nodo<T>> entry : nodos.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" → ");
            List<T> vecinos = obtenerVecinos(entry.getKey());
            sb.append(vecinos).append("\n");
        }
        return sb.toString();
    }
}