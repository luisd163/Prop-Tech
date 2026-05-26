package com.uniquindio.Repositorio;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Persistencia.AsesorPersistencia;

import java.util.HashMap;


public class AsesorRepositorio {

    private final HashMap<String, Asesor> asesores;

    public AsesorRepositorio() {
        this.asesores = AsesorPersistencia.cargar();
    }

    // Persistencia de guardado
    public void guardarAsesores() {
        AsesorPersistencia.guardar(asesores);
    }

    // Devuelve todos los asesores guardados
    public HashMap<String, Asesor> obtenerAsesores() {
        return asesores;
    }

    // Guarda un asesor en la lista de asesores
    public void crearAsesor(Asesor asesor) {
        asesores.put(asesor.getIdentificacion(), asesor);
        guardarAsesores();
    }

    // Elimina un asesor de la lista de asesores usando su identificación
    public void eliminarAsesor(String identificacion) {
        asesores.remove(identificacion);
        guardarAsesores();
    }

    // Devuelve un asesor si existe
    public Asesor obtenerAsesor(String correo) {
        for (Asesor asesor : asesores.values()) {
            if (asesor != null && asesor.getCorreo() != null && asesor.getCorreo().equalsIgnoreCase(correo)) {
                return asesor;
            }
        }
        return null;
    }
    

}
