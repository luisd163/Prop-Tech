package com.uniquindio.Repositorio;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Persistencia.ClientePersistencia;

import java.util.HashMap;

public class ClienteRepositorio {

    private final HashMap<String, Cliente> clientes;

    public ClienteRepositorio() {
        this.clientes = ClientePersistencia.cargar();
    }

    public void guardarClientes() {
        ClientePersistencia.guardar(clientes);
    }

    public HashMap<String, Cliente> obtenerClientes() {
        return clientes;
    }

    public void crearCliente(Cliente cliente) {
        clientes.put(cliente.getIdentificacion(), cliente);
        guardarClientes();
    }

    public void eliminarCliente(String identificacion) {
        clientes.remove(identificacion);
        guardarClientes();
    }

    // Devuelve un cliente si existe
    public Cliente obtenerCliente(String correo) {
        for (Cliente cliente : clientes.values()) {
            if (cliente != null && cliente.getCorreo() != null && cliente.getCorreo().equalsIgnoreCase(correo)) {
                return cliente;
            }
        }
        return null;
    }
    
}
