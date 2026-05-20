package com.uniquindio.Repositorio;

import java.util.HashMap;

import org.springframework.stereotype.Repository;

import com.uniquindio.Model.Inmueble;
import com.uniquindio.Persistencia.InmueblePersistencia;

@Repository
public class InmuebleRepositorio {
    
    private final HashMap<String, Inmueble> inmuebles;

    // constructor
    public InmuebleRepositorio(){
        this.inmuebles = InmueblePersistencia.cargar();
    }

    // persistencia de guardado
    public void guardarInmuebles(){
        InmueblePersistencia.guardar(inmuebles);
    }

    // devuelve los inmuebles guardados
    public HashMap<String, Inmueble> obtenerInmuebles(){
        return inmuebles;
    }

    // guarda un inmueble en la lista de inmuebles
    public void crearInmueble(Inmueble inmueble){
        inmuebles.put(inmueble.getCodigo(), inmueble);
        guardarInmuebles();
    }

    // elimina un inmueble de la lista de inmuebles
    public void eliminarInmueble(String codigo){
        inmuebles.remove(codigo);
        guardarInmuebles();
    }

    // devuelve un inmueble usando su código
    public Inmueble obtenerInmueble(String codigo){
        return inmuebles.get(codigo);
    }


    
}
