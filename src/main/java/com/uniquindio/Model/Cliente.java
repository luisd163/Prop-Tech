package com.uniquindio.Model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente implements Usuario {

	private String identificacion;
	private String nombre;
	private String correo;
	private String telefono;
	private String contrasena;
	private TipoCliente tipoCliente;
	private double presupuesto;
	private List<String> zonasDeInteres;
	private Inmueble.TipoInmueble tipoInmuebleDeseado;
	private int cantidadMinimaHabitaciones;
	private EstadoBusqueda estadoBusqueda;

	@Override
	public String getCorreo() {
		return correo;
	}

	@Override
	public String getNombre() {
		return nombre;
	}

	@Override
	public String getIdentificacion() {
		return identificacion;
	}

	@Override
	public TipoUsuario getTipo() {
		return TipoUsuario.CLIENTE;
	}

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
