package com.uniquindio.Model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visita {

	private String id;
	private Cliente cliente;
	private Inmueble inmueble;
	private LocalDate fecha;
	private LocalTime hora;
	private String asesorId;
	private EstadoVisita estado;
	private String observaciones;

	public enum EstadoVisita {
		PENDIENTE,
		CONFIRMADA,
		REALIZADA,
		CANCELADA,
		REPROGRAMADA
	}
}
