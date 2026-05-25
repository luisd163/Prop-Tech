package com.uniquindio.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GestorAlertas {

    public static List<Alerta> generarAlertasVisitasPendientes(
            List<Visita> listaVisitas
    ) {

        List<Alerta> alertas = new ArrayList<>();

        for (Visita visita : listaVisitas) {

            if (visita.getEstado() == Visita.EstadoVisita.PENDIENTE
                    && (visita.getFecha().isEqual(LocalDate.now())
                    || visita.getFecha().isBefore(LocalDate.now()))) {

                Alerta alerta = Alerta.builder()
                        .id("ALT-VIS-" + visita.getId())
                        .tipo(Alerta.TipoAlerta.VISITA)
                        .nivel(Alerta.NivelAlerta.ALTO)
                        .fecha(LocalDateTime.now())
                        .asesorId(visita.getAsesorId())
                        .entidadRelacionada(visita.getInmueble().getCodigo())
                        .mensaje(
                                "La visita del inmueble "
                                        + visita.getInmueble().getNombre()
                                        + " está pendiente por confirmar."
                        )
                        .build();

                alertas.add(alerta);
            }
        }

        return alertas;
    }

        public static List<Alerta> generarAlertasInmueblesSinVisitas(
            List<Inmueble> listaInmuebles,
            List<Visita> listaVisitas
    ) {

        List<Alerta> alertas = new ArrayList<>();

        for (Inmueble inmueble : listaInmuebles) {

            LocalDate ultimaVisita = null;


            for (Visita visita : listaVisitas) {

                if (visita.getInmueble()
                        .getCodigo()
                        .equals(inmueble.getCodigo())) {

                    if (ultimaVisita == null ||
                            visita.getFecha().isAfter(ultimaVisita)) {

                        ultimaVisita = visita.getFecha();
                    }
                }
            }

            if (ultimaVisita == null) {

                Alerta alerta = Alerta.builder()
                        .id("ALT-INM-" + inmueble.getCodigo())
                        .tipo(Alerta.TipoAlerta.INMUEBLE)
                        .nivel(Alerta.NivelAlerta.MEDIO)
                        .fecha(LocalDateTime.now())
                        .entidadRelacionada(inmueble.getCodigo())
                        .asesorId(inmueble.getCodigoAsesorResponsable())
                        .mensaje(
                                "El inmueble "
                                        + inmueble.getNombre()
                                        + " nunca ha recibido visitas."
                        )
                        .build();

                alertas.add(alerta);
            }

            else {

                long diasSinVisitas =
                        ChronoUnit.DAYS.between(
                                ultimaVisita,
                                LocalDate.now()
                        );

                if (diasSinVisitas > 30) {

                    Alerta alerta = Alerta.builder()
                            .id("ALT-INM-" + inmueble.getCodigo())
                            .tipo(Alerta.TipoAlerta.INMUEBLE)
                            .nivel(Alerta.NivelAlerta.ALTO)
                            .fecha(LocalDateTime.now())
                            .entidadRelacionada(inmueble.getCodigo())
                            .asesorId(inmueble.getCodigoAsesorResponsable())
                            .mensaje(
                                    "El inmueble "
                                            + inmueble.getNombre()
                                            + " lleva "
                                            + diasSinVisitas
                                            + " días sin visitas."
                            )
                            .build();

                    alertas.add(alerta);
                }
            }
        }

        return alertas;
    }

    public static List<Alerta> generarAlertasAltaDemanda(
            List<Inmueble> listaInmuebles,
            List<Cliente> listaClientes
    ) {

        List<Alerta> alertas = new ArrayList<>();

        for (Inmueble inmueble : listaInmuebles) {

            int contadorInteracciones = 0;

         

            for (Cliente cliente : listaClientes) {

                
                if (cliente.getFavoritos() != null) {

                    for (Inmueble favorito : cliente.getFavoritos()) {

                        if (favorito.getCodigo()
                                .equals(inmueble.getCodigo())) {

                            contadorInteracciones++;
                        }
                    }
                }

                if (cliente.getIntenciones() != null) {

                    for (Inmueble intencion : cliente.getIntenciones()) {

                        if (intencion.getCodigo()
                                .equals(inmueble.getCodigo())) {

                            contadorInteracciones++;
                        }
                    }
                }

            

                if (cliente.getHistorialConsultas() != null) {

                    for (Inmueble historial :
                            cliente.getHistorialConsultas()) {

                        if (historial.getCodigo()
                                .equals(inmueble.getCodigo())) {

                            contadorInteracciones++;
                        }
                    }
                }
            }

   

            if (contadorInteracciones >= 5) {

                Alerta alerta = Alerta.builder()
                        .id("ALT-DEM-" + inmueble.getCodigo())
                        .tipo(Alerta.TipoAlerta.INMUEBLE)
                        .nivel(Alerta.NivelAlerta.ALTO)
                        .fecha(LocalDateTime.now())
                        .entidadRelacionada(inmueble.getCodigo())
                        .asesorId(
                                inmueble.getCodigoAsesorResponsable()
                        )
                        .mensaje(
                                "El inmueble "
                                        + inmueble.getNombre()
                                        + " tiene alta demanda con "
                                        + contadorInteracciones
                                        + " interacciones registradas."
                        )
                        .build();

                alertas.add(alerta);
            }
        }

        return alertas;
    }



    public static List<Alerta>
    generarAlertasReservasLargas(
            List<Operacion> listaOperaciones
    ) {

        List<Alerta> alertas = new ArrayList<>();

        for (Operacion operacion : listaOperaciones) {

         

            if (operacion.getInmueble()
                    .getDisponibilidad()
                    == Inmueble.Disponibilidad.RESERVADO) {

               

                if (operacion.getEstado()
                        == Operacion.EstadoOperacion.EN_PROCESO) {

                    long diasReserva =
                            ChronoUnit.DAYS.between(
                                    operacion.getFecha(),
                                    LocalDate.now()
                            );

                    if (diasReserva > 30) {

                        Alerta alerta = Alerta.builder()
                                .id("ALT-RES-" + operacion.getId())
                                .tipo(Alerta.TipoAlerta.OPERACION)
                                .nivel(Alerta.NivelAlerta.CRITICO)
                                .fecha(LocalDateTime.now())
                                .entidadRelacionada(
                                        operacion.getInmueble()
                                                .getCodigo()
                                )
                                .asesorId(
                                        operacion.getAsesor()
                                                .getIdentificacion()
                                )
                                .mensaje(
                                        "El inmueble "
                                                + operacion.getInmueble()
                                                .getNombre()
                                                + " lleva "
                                                + diasReserva
                                                + " días reservado sin cerrar la operación."
                                )
                                .build();

                        alertas.add(alerta);
                    }
                }
            }
        }

        return alertas;
    }



    public static List<Alerta>
    generarAlertasClientesSinSeguimiento(
            List<Cliente> listaClientes
    ) {

        List<Alerta> alertas = new ArrayList<>();

        for (Cliente cliente : listaClientes) {

            boolean sinFavoritos =
                    cliente.getFavoritos() == null
                            || cliente.getFavoritos().isEmpty();

            boolean sinIntenciones =
                    cliente.getIntenciones() == null
                            || cliente.getIntenciones().isEmpty();

            boolean sinHistorial =
                    cliente.getHistorialConsultas() == null
                            || cliente.getHistorialConsultas().isEmpty();

  

            if (sinFavoritos
                    && sinIntenciones
                    && sinHistorial) {

                Alerta alerta = Alerta.builder()
                        .id("ALT-CLI-" +
                                cliente.getIdentificacion())
                        .tipo(Alerta.TipoAlerta.CLIENTE)
                        .nivel(Alerta.NivelAlerta.MEDIO)
                        .fecha(LocalDateTime.now())
                        .entidadRelacionada(
                                cliente.getIdentificacion()
                        )
                        .asesorId(null)
                        .mensaje(
                                "El cliente "
                                        + cliente.getNombre()
                                        + " no tiene seguimiento reciente."
                        )
                        .build();

                alertas.add(alerta);
            }
        }

        return alertas;
    }
}