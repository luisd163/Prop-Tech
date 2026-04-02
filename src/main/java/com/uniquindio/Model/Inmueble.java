package com.uniquindio.Model;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inmueble {

	private String codigo, nombre, direccion, ciudad, barrio, codigoAsesorResponsable;
	private TipoInmueble tipoInmueble;
	private Finalidad finalidad;
	private float precio;
	private double area;
	private int numeroHabitaciones, numeroBanos;
	private EstadoInmueble estadoInmueble;
	private Disponibilidad disponibilidad;
	private String fotoPortada;
	private List<String> fotosGaleria;

	@Override
	public String toString() {
		return "Inmueble {\n" +
				"  Código: " + codigo + "\n" +
				"  Nombre: " + nombre + "\n" +
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
				"  Foto portada: " + fotoPortada + "\n" +
				"  Fotos galería: " + fotosGaleria + "\n" +
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
