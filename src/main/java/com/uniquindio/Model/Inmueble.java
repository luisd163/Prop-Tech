package com.uniquindio.Model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inmueble {

	private String codigo, direccion, ciudad, barrio, codigoAsesorResponsable;
	private TipoInmueble tipoInmueble;
	private Finalidad finalidad;
	private double precio, area;
	private int numeroHabitaciones, numeroBanos;
	private EstadoInmueble estadoInmueble;
	private Disponibilidad disponibilidad;

	@Override
	public String toString() {
		return "Inmueble {\n" +
				"  Código: " + codigo + "\n" +
				"  Dirección: " + direccion + "\n" +
				"  Ciudad: " + ciudad + "\n" +
				"  Barrio: " + barrio + "\n" +
				"  Asesor responsable: " + codigoAsesorResponsable + "\n" +
				"  Tipo inmueble: " + tipoInmueble + "\n" +
				"  Finalidad: " + finalidad + "\n" +
				"  Precio: " + precio + "\n" +
				"  Área: " + area + "\n" +
				"  Número de habitaciones: " + numeroHabitaciones + "\n" +
				"  Número de baños: " + numeroBanos + "\n" +
				"  Estado inmueble: " + estadoInmueble + "\n" +
				"  Disponibilidad: " + disponibilidad + "\n" +
				"}";
	}

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
