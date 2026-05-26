package com.uniquindio.Service;

import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Inmueble.Disponibilidad;
import com.uniquindio.Model.Inmueble.EstadoInmueble;
import com.uniquindio.Model.Inmueble.Finalidad;
import com.uniquindio.Model.Inmueble.TipoInmueble;
import com.uniquindio.Repositorio.InmuebleRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class InmuebleService {
    
    private InmuebleRepositorio inmuebleRepositorio;

    public InmuebleService(){
        this.inmuebleRepositorio = new InmuebleRepositorio();
    }

    // genera un código aleatorio de 4 dígitos que no se repita
    public String generarCodigoUnico4Digitos() {
        final int minimo = 1000;
        final int maximo = 9999;

        // Intentos aleatorios
        for (int i = 0; i < 200; i++) {
            int numero = ThreadLocalRandom.current().nextInt(minimo, maximo + 1);
            String codigo = String.valueOf(numero);
            if (inmuebleRepositorio.obtenerInmueble(codigo) == null) {
                return codigo;
            }
        }

        // Fallback secuencial
        for (int numero = minimo; numero <= maximo; numero++) {
            String codigo = String.valueOf(numero);
            if (inmuebleRepositorio.obtenerInmueble(codigo) == null) {
                return codigo;
            }
        }

        throw new IllegalStateException("No hay códigos disponibles de 4 dígitos.");
    }

    // método que permite registrar un inmueble
    public void registrarInmueble(String codigo, String nombre, String direccion, String ciudad, String barrio, String asesorResponsableString, TipoInmueble tipoInmueble, Finalidad finalidad, float precio, double area, int numeroHabitaciones, int numeroBanos, EstadoInmueble estadoInmueble, Disponibilidad disponibilidad, String fotoPortada, List<String> fotosGaleria){
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

        // Validación de portada y galería (opcionales, pero si vienen deben ser consistentes)
        if (fotosGaleria == null) {
            fotosGaleria = List.of();
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
                .fotoPortada(fotoPortada)
                .fotosGaleria(fotosGaleria)
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

    // obtiene todos los inmuebles registrados
    public List<Inmueble> obtenerTodosInmuebles() {
        return new ArrayList<>(inmuebleRepositorio.obtenerInmuebles().values());
    }

    // obtiene los inmuebles asociados a un asesor
    public List<Inmueble> obtenerInmueblesPorAsesor(String identificacionAsesor) {
        List<Inmueble> inmueblesAsesor = new ArrayList<>();

        if (identificacionAsesor == null || identificacionAsesor.trim().isEmpty()) {
            return inmueblesAsesor;
        }

        for (Inmueble inmueble : inmuebleRepositorio.obtenerInmuebles().values()) {
            if (inmueble != null && identificacionAsesor.equals(inmueble.getCodigoAsesorResponsable())) {
                inmueblesAsesor.add(inmueble);
            }
        }

        return inmueblesAsesor;
    }

    // cantidad de inmuebles disponibles
    public int cantInmueblesDisponibles(){
        return cantInmueblesDisponibles(new ArrayList<>(inmuebleRepositorio.obtenerInmuebles().values()));
    }

    // cantidad de inmuebles disponibles para una lista dada
    public int cantInmueblesDisponibles(List<Inmueble> inmuebles){
        int cant = 0;
        for (Inmueble inmueble : inmuebles) {
            if (inmueble != null && inmueble.getDisponibilidad() == Disponibilidad.DISPONIBLE) {
                cant++;
            }
        }
        return cant;
    }

    // cantidad de inmuebles no disponibles
    public int cantInmueblesNoDisponibles(){
        return cantInmueblesNoDisponibles(new ArrayList<>(inmuebleRepositorio.obtenerInmuebles().values()));
    }

    // cantidad de inmuebles no disponibles para una lista dada
    public int cantInmueblesNoDisponibles(List<Inmueble> inmuebles){
        int cant = 0;
        for (Inmueble inmueble : inmuebles) {
            if (inmueble != null && inmueble.getDisponibilidad() == Disponibilidad.NO_DISPONIBLE) {
                cant++;
            }
        }
        return cant;
    }

    // cantidad de inmuebles reservados
    public int cantInmueblesReservados(){
        return cantInmueblesReservados(new ArrayList<>(inmuebleRepositorio.obtenerInmuebles().values()));
    }

    // cantidad de inmuebles reservados para una lista dada
    public int cantInmueblesReservados(List<Inmueble> inmuebles){
        int cant = 0;
        for (Inmueble inmueble : inmuebles) {
            if (inmueble != null && inmueble.getDisponibilidad() == Disponibilidad.RESERVADO) {
                cant++;
            }
        }
        return cant;
    }

}
