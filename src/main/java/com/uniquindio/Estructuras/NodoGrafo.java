package com.uniquindio.Estructuras;

import java.util.ArrayList;
import java.util.List;

import com.uniquindio.Model.Inmueble;

public class NodoGrafo {

    private Inmueble inmueble;
    private List<NodoGrafo> conexiones;

    public NodoGrafo(Inmueble inmueble) {
        this.inmueble = inmueble;
        this.conexiones = new ArrayList<>();
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public List<NodoGrafo> getConexiones() {
        return conexiones;
    }

    public void conectar(NodoGrafo nodo) {
        if (!conexiones.contains(nodo)) {
            conexiones.add(nodo);
        }
    }
    }