package com.uniquindio.Service;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.VisitaRepositorio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class VisitaService {

    public List<Visita> obtenerVisitasPorAsesor(String asesorId) {
        VisitaRepositorio repositorio = new VisitaRepositorio();
        List<Visita> resultado = new ArrayList<>();

        for (Visita visita : repositorio.obtenerVisitas().values()) {
            if (visita != null && Objects.equals(visita.getAsesorId(), asesorId)) {
                resultado.add(visita);
            }
        }

        return resultado;
    }

    public List<Cliente> obtenerClientesPorAsesor(String asesorId) {
        List<Cliente> clientes = new ArrayList<>();
        List<Visita> visitas = obtenerVisitasPorAsesor(asesorId);

        for (Visita visita : visitas) {
            if (visita == null || visita.getCliente() == null || visita.getCliente().getIdentificacion() == null) {
                continue;
            }

            if (!Objects.equals(visita.getAsesorId(), asesorId)) {
                continue;
            }

            if (visita.getInmueble() != null
                    && visita.getInmueble().getCodigoAsesorResponsable() != null
                    && !Objects.equals(visita.getInmueble().getCodigoAsesorResponsable(), asesorId)) {
                continue;
            }

            String idCliente = visita.getCliente().getIdentificacion();
            boolean existe = clientes.stream()
                    .anyMatch(c -> c != null && idCliente.equals(c.getIdentificacion()));

            if (!existe) {
                clientes.add(visita.getCliente());
            }
        }

        return clientes;
    }

    public List<Visita> filtrarPorEstado(List<Visita> visitas, Visita.EstadoVisita estado) {
        if (estado == null) {
            return visitas;
        }

        List<Visita> filtradas = new ArrayList<>();
        for (Visita visita : visitas) {
            if (visita != null && visita.getEstado() == estado) {
                filtradas.add(visita);
            }
        }
        return filtradas;
    }

    public List<Visita> filtrarEstaSemana(List<Visita> visitas) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY);

        List<Visita> filtradas = new ArrayList<>();
        for (Visita visita : visitas) {
            if (visita == null || visita.getFecha() == null) {
                continue;
            }
            if (!visita.getFecha().isBefore(inicioSemana) && !visita.getFecha().isAfter(finSemana)) {
                filtradas.add(visita);
            }
        }
        return filtradas;
    }

    public List<Visita> buscarPorClienteOInmueble(List<Visita> visitas, String query) {
        if (query == null || query.isBlank()) {
            return visitas;
        }

        String q = query.trim().toLowerCase();
        List<Visita> filtradas = new ArrayList<>();

        for (Visita visita : visitas) {
            if (visita == null) {
                continue;
            }

            String cliente = (visita.getCliente() != null && visita.getCliente().getNombre() != null)
                    ? visita.getCliente().getNombre().toLowerCase() : "";
            String inmueble = (visita.getInmueble() != null && visita.getInmueble().getNombre() != null)
                    ? visita.getInmueble().getNombre().toLowerCase() : "";

            if (cliente.contains(q) || inmueble.contains(q)) {
                filtradas.add(visita);
            }
        }

        return filtradas;
    }

    public int contarVisitasMes(String asesorId) {
        LocalDate hoy = LocalDate.now();
        int count = 0;

        for (Visita visita : obtenerVisitasPorAsesor(asesorId)) {
            if (visita != null && visita.getFecha() != null
                    && visita.getFecha().getYear() == hoy.getYear()
                    && visita.getFecha().getMonthValue() == hoy.getMonthValue()) {
                count++;
            }
        }

        return count;
    }

    public int contarPorEstado(String asesorId, Visita.EstadoVisita estado) {
        int count = 0;
        for (Visita visita : obtenerVisitasPorAsesor(asesorId)) {
            if (visita != null && visita.getEstado() == estado) {
                count++;
            }
        }
        return count;
    }

    public void actualizarEstado(String visitaId, Visita.EstadoVisita estadoNuevo) {
        VisitaRepositorio repositorio = new VisitaRepositorio();
        Visita visita = repositorio.obtenerVisita(visitaId);

        if (visita == null) {
            throw new IllegalArgumentException("Visita no encontrada");
        }

        visita.setEstado(estadoNuevo);
        repositorio.actualizarVisita(visita);
    }

    // Crear visita
    public void crearVisita(String id,
                            Cliente cliente,
                            Inmueble inmueble,
                            LocalDate fecha,
                            LocalTime hora,
                            String asesorId,
                            Visita.EstadoVisita estado,
                            String observaciones) {

        if (asesorId == null || asesorId.isBlank()) {
            throw new IllegalArgumentException("La visita debe tener asesorId");
        }

        if (cliente == null) {
            throw new IllegalArgumentException("La visita debe tener cliente");
        }

        if (inmueble == null) {
            throw new IllegalArgumentException("La visita debe tener inmueble");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La visita debe tener fecha");
        }

        if (hora == null) {
            throw new IllegalArgumentException("La visita debe tener hora");
        }

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        if (estado == null) {
            estado = Visita.EstadoVisita.PENDIENTE;
        }

        Visita visita = Visita.builder()
                .id(id)
                .cliente(cliente)
                .inmueble(inmueble)
                .fecha(fecha)
                .hora(hora)
                .asesorId(asesorId)
                .estado(estado)
                .observaciones(observaciones)
                .build();

        VisitaRepositorio repositorio = new VisitaRepositorio();
        repositorio.crearVisita(visita);
    }

    // Cancelar visita
    public void cancelarVisita(String visitaId) {
        if (visitaId == null || visitaId.isBlank()) {
            throw new IllegalArgumentException("El id de la visita es obligatorio");
        }

        VisitaRepositorio repositorio = new VisitaRepositorio();
        Visita visita = repositorio.obtenerVisita(visitaId);

        if (visita == null) {
            throw new IllegalArgumentException("Visita no encontrada");
        }

        visita.setEstado(Visita.EstadoVisita.CANCELADA);
        repositorio.actualizarVisita(visita);
    }

    // Modificar visita
    public void modificarVisita(Visita visitaActualizada) {
        if (visitaActualizada == null || visitaActualizada.getId() == null || visitaActualizada.getId().isBlank()) {
            throw new IllegalArgumentException("La visita a modificar debe tener id");
        }

        VisitaRepositorio repositorio = new VisitaRepositorio();
        Visita visitaActual = repositorio.obtenerVisita(visitaActualizada.getId());

        if (visitaActual == null) {
            throw new IllegalArgumentException("Visita no encontrada");
        }

        visitaActual.setCliente(visitaActualizada.getCliente() != null ? visitaActualizada.getCliente() : visitaActual.getCliente());
        visitaActual.setInmueble(visitaActualizada.getInmueble() != null ? visitaActualizada.getInmueble() : visitaActual.getInmueble());
        visitaActual.setFecha(visitaActualizada.getFecha() != null ? visitaActualizada.getFecha() : visitaActual.getFecha());
        visitaActual.setHora(visitaActualizada.getHora() != null ? visitaActualizada.getHora() : visitaActual.getHora());
        visitaActual.setObservaciones(visitaActualizada.getObservaciones() != null ? visitaActualizada.getObservaciones() : visitaActual.getObservaciones());
        visitaActual.setEstado(visitaActualizada.getEstado() != null ? visitaActualizada.getEstado() : visitaActual.getEstado());

        repositorio.actualizarVisita(visitaActual);
    }
}
