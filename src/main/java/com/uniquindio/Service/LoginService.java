package com.uniquindio.Service;

import com.uniquindio.Model.Usuario;
import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Repositorio.*;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginService {

    private AsesorRepositorio asesorRepositorio;
    private ClienteRepositorio clienteRepositorio;

    /**
     * Autentica un usuario (Cliente o Asesor) por correo y contraseña
     * 
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return Usuario autenticado (Cliente o Asesor) o null si no se encuentra
     */
    public Usuario iniciarSesion(String correo, String contrasena) {
        // Intentar buscar como Cliente
        Cliente cliente = clienteRepositorio.obtenerCliente(correo);
        if (cliente != null && validarContrasena(cliente.getContrasena(), contrasena)) {
            return cliente; // Cliente implementa Usuario
        }

        // Intentar buscar como Asesor
        Asesor asesor = asesorRepositorio.obtenerAsesor(correo);
        if (asesor != null && validarContrasena(asesor.getContrasena(), contrasena)) {
            return asesor; // Asesor implementa Usuario
        }

        return null; // Usuario no encontrado o contraseña incorrecta
    }

    /**
     * Valida que una contraseña coincida con otra
     * 
     * @param contrasenaAlmacenada Contraseña almacenada
     * @param contrasenaIngresada Contraseña ingresada por el usuario
     * @return true si coincide, false en caso contrario
     */
    private boolean validarContrasena(String contrasenaAlmacenada, String contrasenaIngresada) {
        return contrasenaAlmacenada.equals(contrasenaIngresada);
    }
}