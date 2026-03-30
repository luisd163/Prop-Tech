package com.uniquindio.Model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asesor {

	private Cliente cliente;
	private Inmueble inmueble;
	private LocalDate fecha;
	private LocalTime hora;
	private String asesorAsignado;
	private EstadoVisita estadoVisita;
	private String observacionesPosteriores;

	public enum EstadoVisita {
		PENDIENTE,
		CONFIRMADA,
		REALIZADA,
		CANCELADA,
		REPROGRAMADA
	}
}
