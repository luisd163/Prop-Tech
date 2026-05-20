package com.uniquindio.Service;

import com.uniquindio.Estructuras.GrafoInmuebles;
import com.uniquindio.Model.Inmueble;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class GrafoService {

    public GrafoInmuebles construirGrafo(HashMap<String, Inmueble> inmuebles, String ciudad) {

        GrafoInmuebles grafo = new GrafoInmuebles();

        grafo.conectarPorCiudad(inmuebles, ciudad);

        return grafo;
    }
}