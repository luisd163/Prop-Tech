package com.uniquindio.Model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

	private String identificacion;
	private String nombre;
	private String correo;
	private String telefono;
	private TipoCliente tipoCliente;
	private double presupuesto;
	private List<String> zonasDeInteres;
	private Inmueble.TipoInmueble tipoInmuebleDeseado;
	private int cantidadMinimaHabitaciones;
	private EstadoBusqueda estadoBusqueda;

	public enum TipoCliente {
		PERSONA_NATURAL,
		EMPRESA,
		INVERSIONISTA
	}

	public enum EstadoBusqueda {
		ACTIVA,
		PAUSADA,
		FINALIZADA
	}
}
