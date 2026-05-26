package com.uniquindio.Service;

import com.uniquindio.Model.Usuario;
import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Repositorio.*;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class LoginService {

    /**
     * Autentica un usuario (Cliente o Asesor) por correo y contraseña
     * 
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña del usuario
     * @return Usuario autenticado (Cliente o Asesor) o null si no se encuentra
     */
    public Usuario iniciarSesion(String correo, String contrasena) {
        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        AsesorRepositorio asesorRepositorio = new AsesorRepositorio();

        // Intentar buscar como Cliente
        Cliente cliente = clienteRepositorio.obtenerCliente(correo);
        if (cliente != null && validarContrasena(cliente.getContrasena(), contrasena)) {
            return cliente; // Cliente implementa Usuario
        }

        // Intentar buscar como Asesor
        Asesor asesor = asesorRepositorio.obtenerAsesor(correo);
        if (asesor != null
                && asesor.getCorreo().equalsIgnoreCase(correo)
                && validarContrasena(asesor.getContrasena(), contrasena)) {
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
        return contrasenaAlmacenada != null && contrasenaAlmacenada.equals(contrasenaIngresada);
    }
}