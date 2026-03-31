package com.uniquindio.Repositorio;

import java.util.ArrayList;
import java.util.List;

import com.uniquindio.Model.Alerta;

public class AlertaRepositorio {

    private final List<Alerta> alertas = new ArrayList<>();
    
    public void obtenerAlerta(){
        // implementar
    }

    public void guardarAlerta(Alerta alerta){
        if (alerta != null) {
            alertas.add(alerta);
        }
    }

    public List<Alerta> obtenerAlertas(){
        return new ArrayList<>(alertas);
    }
}
