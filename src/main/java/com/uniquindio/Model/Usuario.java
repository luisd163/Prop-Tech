package com.uniquindio.Model;

/**
 * Interfaz que define los atributos comunes de usuarios (Asesor y Cliente)
 */
public interface Usuario {
    
    String getCorreo();
    
    String getNombre();
    
    String getIdentificacion();
    
    TipoUsuario getTipo();
    
    enum TipoUsuario {
        CLIENTE,
        ASESOR
    }
}
