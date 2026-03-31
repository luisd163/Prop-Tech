package com.uniquindio.Controller;

import com.uniquindio.Prop_Tech.PropTechApplication;

import lombok.Getter;

@Getter
public class ControladorPrincipal {

    // Instancia del singleton del controlador
    private static ControladorPrincipal instancia;

    // Instancia de la clase Principal del proyecto
    private final PropTechApplication propTech;;

    // Constructor del controlador
    private ControladorPrincipal() {
        this.propTech = new PropTechApplication();
    }

    // Método que obtiene la instancia del singleton del controlador
    public static ControladorPrincipal getInstancia() {
        if (instancia == null) {
            instancia = new ControladorPrincipal();
        }
        return instancia;
    }
}
