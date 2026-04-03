package com.uniquindio.Model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
	private transient List<Operacion> operaciones;

	public Asesor(String identificacion, String nombre, String correo, String telefono, String contrasena, String especialidad, double comisionPorcentaje) {
		this.identificacion = identificacion;
		this.nombre = nombre;
		this.correo = correo;
		this.telefono = telefono;
		this.contrasena = contrasena;
		this.especialidad = especialidad;
		this.comisionPorcentaje = comisionPorcentaje;
		this.operaciones = new ArrayList<>();
	}

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
