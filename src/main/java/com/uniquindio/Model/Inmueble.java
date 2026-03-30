package com.uniquindio.Model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inmueble {

	private String codigo;
	private String direccion;
	private String ciudad;
	private String barrio;
	private TipoInmueble tipoInmueble;
	private Finalidad finalidad;
	private double precio;
	private double area;
	private int numeroHabitaciones;
	private int numeroBanos;
	private EstadoInmueble estadoInmueble;
	private Disponibilidad disponibilidad;
	private String asesorResponsable;

	public enum TipoInmueble {
		APARTAMENTO,
		CASA,
		LOCAL_COMERCIAL,
		OFICINA
	}

	public enum Finalidad {
		VENTA,
		ARRIENDO
	}

	public enum EstadoInmueble {
		NUEVO,
		USADO,
		EN_REMODELACION
	}

	public enum Disponibilidad {
		DISPONIBLE,
		NO_DISPONIBLE,
		RESERVADO
	}
}
