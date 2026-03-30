package com.uniquindio.Model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

	private String id;
	private TipoAlerta tipo;
	private String mensaje;
	private NivelAlerta nivel;
	private LocalDateTime fecha;
	private String entidadRelacionada;

	public enum TipoAlerta {
		VISITA,
		OPERACION,
		INMUEBLE,
		CLIENTE,
		SISTEMA
	}

	public enum NivelAlerta {
		BAJO,
		MEDIO,
		ALTO,
		CRITICO
	}
}
