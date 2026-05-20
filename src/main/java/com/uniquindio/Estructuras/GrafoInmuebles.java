package com.uniquindio.Estructuras;

import java.util.*;
import com.uniquindio.Model.Inmueble;

public class GrafoInmuebles {

    private Map<String, NodoGrafo> nodos;

    public GrafoInmuebles() {
        nodos = new HashMap<>();
    }

    // Agregar vértice
    public void agregarInmueble(Inmueble inmueble) {
        nodos.putIfAbsent(inmueble.getCodigo(), new NodoGrafo(inmueble));
    }

    // Conectar inmuebles
    public void conectarInmuebles(String codigo1, String codigo2) {

        NodoGrafo nodo1 = nodos.get(codigo1);
        NodoGrafo nodo2 = nodos.get(codigo2);

        if (nodo1 != null && nodo2 != null) {
            nodo1.conectar(nodo2);
            nodo2.conectar(nodo1);
        }
    }

    // Crear conexiones automáticas por ciudad
    public void conectarPorCiudad(HashMap<String, Inmueble> inmuebles, String ciudad) {

        List<Inmueble> inmueblesCiudad = new ArrayList<>();
        for (Inmueble inmueble : inmuebles.values()) {

            if (inmueble.getCiudad().equalsIgnoreCase(ciudad)) {

                agregarInmueble(inmueble);
                inmueblesCiudad.add(inmueble);
            }
        }

        // Conectar todos entre sí
        for (int i = 0; i < inmueblesCiudad.size(); i++) {

            for (int j = i + 1; j < inmueblesCiudad.size(); j++) {

                conectarInmuebles(
                        inmueblesCiudad.get(i).getCodigo(),
                        inmueblesCiudad.get(j).getCodigo()
                );
            }
        }
    }
    // Mostrar grafo
    public String mostrarGrafo() {

        StringBuilder sb = new StringBuilder();

        for (NodoGrafo nodo : nodos.values()) {

            sb.append("\n");
            sb.append(nodo.getInmueble().getNombre());
            sb.append(" -> ");

            for (NodoGrafo conexion : nodo.getConexiones()) {
                sb.append(conexion.getInmueble().getNombre())
                        .append(" | ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
    public List<String> recorridoBFS(String codigoInicio) {

        List<String> recorrido = new ArrayList<>();

        NodoGrafo inicio = nodos.get(codigoInicio);

        if (inicio == null) {
            return recorrido;
        }

        Queue<NodoGrafo> cola = new LinkedList<>();
        Set<NodoGrafo> visitados = new HashSet<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {

            NodoGrafo actual = cola.poll();

            recorrido.add(actual.getInmueble().getNombre());

            for (NodoGrafo vecino : actual.getConexiones()) {

                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }

        return recorrido;
    }
}