package com.uniquindio.Repositorio;

import com.uniquindio.Model.Visita;
import com.uniquindio.Persistencia.VisitaPersistencia;

import java.util.HashMap;

public class VisitaRepositorio {

    private final HashMap<String, Visita> visitas;

    public VisitaRepositorio() {
        this.visitas = VisitaPersistencia.cargar();
    }

    public void guardarVisitas() {
        VisitaPersistencia.guardar(visitas);
    }

    public HashMap<String, Visita> obtenerVisitas() {
        return visitas;
    }

    public void crearVisita(Visita visita) {
        if (visita == null || visita.getId() == null || visita.getId().isBlank()) {
            throw new IllegalArgumentException("La visita debe tener id.");
        }
        visitas.put(visita.getId(), visita);
        guardarVisitas();
    }

    public void actualizarVisita(Visita visita) {
        if (visita == null || visita.getId() == null || visita.getId().isBlank()) {
            throw new IllegalArgumentException("La visita debe tener id.");
        }
        visitas.put(visita.getId(), visita);
        guardarVisitas();
    }

    public Visita obtenerVisita(String id) {
        return visitas.get(id);
    }

    public void eliminarVisita(String id) {
        visitas.remove(id);
        guardarVisitas();
    }
}
