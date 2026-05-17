package com.uniquindio.Repositorio;

import com.uniquindio.Estructuras.ArrayListPropio;
import com.uniquindio.Model.Alerta;

public class AlertaRepositorio {

    private final ArrayListPropio<Alerta> alertas = new ArrayListPropio<>();
    
    public void obtenerAlerta(){
        // implementar
    }

    public void guardarAlerta(Alerta alerta){
        if (alerta != null) {
            alertas.agregar(alerta);
        }
    }

    public ArrayListPropio<Alerta> obtenerAlertas(){
        ArrayListPropio<Alerta> copia = new ArrayListPropio<>();
        for (Alerta alerta : alertas) {
            copia.agregar(alerta);
        }
        return copia;
    }
}
