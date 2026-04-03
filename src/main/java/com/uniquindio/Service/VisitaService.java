package com.uniquindio.Service;

import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.VisitaRepositorio;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    public void crearVisita(Visita visita) {
        if (visita.getId() == null || visita.getId().isBlank()) {
            visita.setId(UUID.randomUUID().toString());
        }
        if (visita.getEstado() == null) {
            visita.setEstado(Visita.EstadoVisita.PENDIENTE);
        }

        VisitaRepositorio repositorio = new VisitaRepositorio();
        repositorio.crearVisita(visita);
    }
}
