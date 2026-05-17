package com.uniquindio.Service;

import com.uniquindio.Estructuras.GrafoInmuebles;
import com.uniquindio.Model.Inmueble;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrafoService {

    public GrafoInmuebles construirGrafo(List<Inmueble> inmuebles, String ciudad) {

        GrafoInmuebles grafo = new GrafoInmuebles();

        grafo.conectarPorCiudad(inmuebles, ciudad);

        return grafo;
    }
}