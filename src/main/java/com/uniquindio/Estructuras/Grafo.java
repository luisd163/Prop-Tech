package com.uniquindio.Estructuras;
/**
 * Grafo dirigido/no dirigido con lista de adyacencia.
 * Implementado con TablaHash<K, LinkedList>.
 *
 * PRINCIPIO:
 *   - Vértices: String (clientes, inmuebles, zonas)
 *   - Aristas: relación con tipo y peso
 *
 * USOS EN PROPTTECH:
 *   - Recomendación de inmuebles
 *   - Análisis de clientes
 *   - Detección de comportamiento inusual
 *   - Relación entre zonas e inmuebles
 *   - Sistemas de recomendación colaborativa
 */
public class Grafo {

    // ─────────────────────────────────────────────
    // ARISTA
    // ─────────────────────────────────────────────

    public static class Arista {
        public String destino;
        public String tipo;
        public int peso;

        public Arista(String destino, String tipo, int peso) {
            this.destino = destino;
            this.tipo = tipo;
            this.peso = peso;
        }

        @Override
        public String toString() {
            return "─[" + tipo + ", p=" + peso + "]→ " + destino;
        }
    }

    // ─────────────────────────────────────────────
    // ATRIBUTOS
    // ─────────────────────────────────────────────

    private final TablaHash<String, LinkedListPropia<Arista>> adyacencia;
    private final boolean dirigido;

    private int vertices;
    private int aristas;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────

    public Grafo(boolean dirigido) {
        this.adyacencia = new TablaHash<>();
        this.dirigido = dirigido;
        this.vertices = 0;
        this.aristas = 0;
    }

    // ─────────────────────────────────────────────
    // VÉRTICES
    // ─────────────────────────────────────────────

    public void agregarVertice(String id) {
        if (!adyacencia.contiene(id)) {
            adyacencia.insertar(id, new LinkedListPropia<>());
            vertices++;
        }
    }

    public boolean existeVertice(String id) {
        return adyacencia.contiene(id);
    }

    public void eliminarVertice(String id) {
        if (!adyacencia.contiene(id)) return;

        adyacencia.eliminar(id);
        vertices--;

        ArrayListPropio<String> claves = adyacencia.claves();

        for (int i = 0; i < claves.tamanio(); i++) {
            String v = claves.obtener(i);
            LinkedListPropia<Arista> lista = adyacencia.buscar(v);
            int eliminadas = lista.eliminarSi(a -> a.destino.equals(id));
            aristas -= eliminadas;
        }
    }

    // ─────────────────────────────────────────────
    // ARISTAS
    // ─────────────────────────────────────────────

    public void agregarArista(String origen, String destino, String tipo) {
        agregarArista(origen, destino, tipo, 1);
    }

    public void agregarArista(String origen, String destino, String tipo, int peso) {

        agregarVertice(origen);
        agregarVertice(destino);

        LinkedListPropia<Arista> lista = adyacencia.buscar(origen);

        Arista existente = lista.buscar(
                a -> a.destino.equals(destino) && a.tipo.equals(tipo)
        );

        if (existente != null) {
            existente.peso += peso;
            return;
        }

        lista.agregarAlFinal(new Arista(destino, tipo, peso));
        aristas++;

        if (!dirigido) {
            LinkedListPropia<Arista> inversa = adyacencia.buscar(destino);
            inversa.agregarAlFinal(new Arista(origen, tipo, peso));
        }
    }

    public boolean eliminarArista(String origen, String destino, String tipo) {
        LinkedListPropia<Arista> lista = adyacencia.buscar(origen);
        if (lista == null) return false;

        int eliminadas = lista.eliminarSi(
                a -> a.destino.equals(destino) && a.tipo.equals(tipo)
        );

        if (eliminadas > 0) {
            aristas--;

            if (!dirigido) {
                LinkedListPropia<Arista> inv = adyacencia.buscar(destino);
                inv.eliminarSi(a -> a.destino.equals(origen) && a.tipo.equals(tipo));
            }
            return true;
        }

        return false;
    }

    public boolean existeArista(String origen, String destino) {
        LinkedListPropia<Arista> lista = adyacencia.buscar(origen);
        return lista != null && lista.buscar(a -> a.destino.equals(destino)) != null;
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    public ArrayListPropio<String> vecinos(String id) {
        ArrayListPropio<String> res = new ArrayListPropio<>();
        LinkedListPropia<Arista> lista = adyacencia.buscar(id);

        if (lista == null) return res;

        for (int i = 0; i < lista.tamanio(); i++) {
            res.agregar(lista.obtener(i).destino);
        }

        return res;
    }

    public int grado(String id) {
        LinkedListPropia<Arista> lista = adyacencia.buscar(id);
        return lista != null ? lista.tamanio() : 0;
    }

    // ─────────────────────────────────────────────
    // BFS
    // ─────────────────────────────────────────────

    public ArrayListPropio<String> bfs(String inicio) {

        ArrayListPropio<String> visitados = new ArrayListPropio<>();
        if (!existeVertice(inicio)) return visitados;

        TablaHash<String, Boolean> marcado = new TablaHash<>();
        ColaPropia<String> cola = new ColaPropia<>();

        cola.encolar(inicio);
        marcado.insertar(inicio, true);

        while (!cola.estaVacia()) {

            String actual = cola.desencolar();
            visitados.agregar(actual);

            LinkedListPropia<Arista> lista = adyacencia.buscar(actual);
            if (lista == null) continue;

            for (int i = 0; i < lista.tamanio(); i++) {

                String v = lista.obtener(i).destino;

                if (!marcado.contiene(v)) {
                    marcado.insertar(v, true);
                    cola.encolar(v);
                }
            }
        }

        return visitados;
    }

    // ─────────────────────────────────────────────
    // DFS
    // ─────────────────────────────────────────────

    public ArrayListPropio<String> dfs(String inicio) {
        ArrayListPropio<String> res = new ArrayListPropio<>();
        TablaHash<String, Boolean> marcado = new TablaHash<>();
        dfsRec(inicio, marcado, res);
        return res;
    }

    private void dfsRec(String actual,
                        TablaHash<String, Boolean> marcado,
                        ArrayListPropio<String> res) {

        if (!existeVertice(actual)) return;

        marcado.insertar(actual, true);
        res.agregar(actual);

        LinkedListPropia<Arista> lista = adyacencia.buscar(actual);
        if (lista == null) return;

        for (int i = 0; i < lista.tamanio(); i++) {
            String v = lista.obtener(i).destino;

            if (!marcado.contiene(v)) {
                dfsRec(v, marcado, res);
            }
        }
    }

    // ─────────────────────────────────────────────
    // ANÁLISIS
    // ─────────────────────────────────────────────

    public ArrayListPropio<String> nodosConAltaConexion(int umbral) {
        ArrayListPropio<String> res = new ArrayListPropio<>();
        ArrayListPropio<String> claves = adyacencia.claves();

        for (int i = 0; i < claves.tamanio(); i++) {
            String v = claves.obtener(i);
            if (grado(v) >= umbral) res.agregar(v);
        }

        return res;
    }

    public boolean estanConectados(String a, String b) {
        ArrayListPropio<String> r = bfs(a);
        return r.contiene(b);
    }

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────

    public int numVertices() { return vertices; }

    public int numAristas() { return aristas; }

    public boolean esDirigido() { return dirigido; }

    public ArrayListPropio<String> vertices() {
        return adyacencia.claves();
    }

    public void limpiar() {
        ArrayListPropio<String> claves = adyacencia.claves();

        for (int i = 0; i < claves.tamanio(); i++) {
            adyacencia.eliminar(claves.obtener(i));
        }

        vertices = 0;
        aristas = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        ArrayListPropio<String> claves = adyacencia.claves();

        for (int i = 0; i < claves.tamanio(); i++) {
            String v = claves.obtener(i);
            sb.append(v)
              .append(" → ")
              .append(adyacencia.buscar(v))
              .append("\n");
        }

        return sb.toString();
    }
}
