package com.uniquindio.Model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operacion {

	private String id;
	private Inmueble inmueble;
	private Cliente cliente;
	private String asesor;
	private LocalDate fecha;
	private TipoOperacion tipoOperacion;
	private double valorAcordado;
	private double comision;
	private EstadoOperacion estado;

	public enum TipoOperacion {
		VENTA,
		ARRIENDO
	}

	public enum EstadoOperacion {
		EN_PROCESO,
		FINALIZADA,
		CANCELADA
	}
}
