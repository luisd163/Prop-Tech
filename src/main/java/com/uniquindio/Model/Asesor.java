package com.uniquindio.Model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asesor implements Usuario {

	private String identificacion;
	private String nombre;
	private String correo;
	private String telefono;
	private String contrasena;
	private String especialidad;
	private double comisionPorcentaje;

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
		return TipoUsuario.ASESOR;
	}
}
