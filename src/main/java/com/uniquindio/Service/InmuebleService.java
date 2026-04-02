package com.uniquindio.Service;

import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Inmueble.Disponibilidad;
import com.uniquindio.Model.Inmueble.EstadoInmueble;
import com.uniquindio.Model.Inmueble.Finalidad;
import com.uniquindio.Model.Inmueble.TipoInmueble;
import com.uniquindio.Repositorio.InmuebleRepositorio;

public class InmuebleService {
    
    private InmuebleRepositorio inmuebleRepositorio;

    public InmuebleService(){
        this.inmuebleRepositorio = new InmuebleRepositorio();
    }

    // método que permite registrar un inmueble
    public void registrarInmueble(String codigo, String nombre, String direccion, String ciudad, String barrio, String asesorResponsableString, TipoInmueble tipoInmueble, Finalidad finalidad, float precio, double area, int numeroHabitaciones, int numeroBanos, EstadoInmueble estadoInmueble, Disponibilidad disponibilidad){
        // Validación de código
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del inmueble es requerido");
        }

        // Validación de nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del inmueble es requerido");
        }

        // Validación de dirección
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es requerida");
        }

        // Validación de ciudad
        if (ciudad == null || ciudad.trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad es requerida");
        }

        // Validación de barrio
        if (barrio == null || barrio.trim().isEmpty()) {
            throw new IllegalArgumentException("El barrio es requerido");
        }

        // Validación de asesor responsable
        if (asesorResponsableString == null || asesorResponsableString.trim().isEmpty()) {
            throw new IllegalArgumentException("El asesor responsable es requerido");
        }

        // Validación de tipo de inmueble
        if (tipoInmueble == null) {
            throw new IllegalArgumentException("El tipo de inmueble es requerido");
        }

        // Validación de finalidad
        if (finalidad == null) {
            throw new IllegalArgumentException("La finalidad (venta/arriendo) es requerida");
        }

        // Validación de precio
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // Validación de área
        if (area <= 0) {
            throw new IllegalArgumentException("El área debe ser mayor a 0");
        }

        // Validación de número de habitaciones
        if (numeroHabitaciones < 0) {
            throw new IllegalArgumentException("El número de habitaciones no puede ser negativo");
        }

        // Validación de número de baños
        if (numeroBanos < 0) {
            throw new IllegalArgumentException("El número de baños no puede ser negativo");
        }

        // Validación de estado del inmueble
        if (estadoInmueble == null) {
            throw new IllegalArgumentException("El estado del inmueble es requerido");
        }

        // Validación de disponibilidad
        if (disponibilidad == null) {
            throw new IllegalArgumentException("La disponibilidad es requerida");
        }

        // Crear el inmueble usando builder
        Inmueble inmueble = Inmueble.builder()
                .codigo(codigo)
                .nombre(nombre)
                .direccion(direccion)
                .ciudad(ciudad)
                .barrio(barrio)
                .codigoAsesorResponsable(asesorResponsableString)
                .tipoInmueble(tipoInmueble)
                .finalidad(finalidad)
                .precio(precio)
                .area(area)
                .numeroHabitaciones(numeroHabitaciones)
                .numeroBanos(numeroBanos)
                .estadoInmueble(estadoInmueble)
                .disponibilidad(disponibilidad)
                .build();

        // No agrega el inmueble si ya hay uno con ese código
        if (inmuebleRepositorio.obtenerInmueble(codigo) != null) {
            throw new IllegalArgumentException("Ya hay un inmueble registrado con este código.");
        }

        // Lo agrego al repositorio de inmuebles
        inmuebleRepositorio.crearInmueble(inmueble);
    }

    // método para eliminar un inmueble
    public void eliminarInmueble(String codigo){
        if (inmuebleRepositorio.obtenerInmuebles().containsKey(codigo)) {
            inmuebleRepositorio.eliminarInmueble(codigo);
        } else {
            throw new IllegalArgumentException("No existe ningún inmueble con este código: " + codigo + ".");
        }
    }

    // método que devuelve un inmueble por su código
    public Inmueble obtenerInmueble(String codigo){
        if (inmuebleRepositorio.obtenerInmuebles().containsKey(codigo)) {
            return inmuebleRepositorio.obtenerInmueble(codigo);
        } else {
            return null;
        }
    }

    // método para consultar la información de un inmueble
    public String consultarInmueble(String codigo){
        Inmueble inmueble = obtenerInmueble(codigo);
        if (inmueble == null) {
            return "No hay información para mostrar";
        } else{
            return inmueble.toString();
        }
    }

    // método para modificar inmuebles
    // Falta implementar
    public void modificarInmueble(){}

}
