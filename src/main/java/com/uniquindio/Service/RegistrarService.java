package com.uniquindio.Service;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Repositorio.AsesorRepositorio;
import com.uniquindio.Repositorio.ClienteRepositorio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RegistrarService {

	private final ClienteRepositorio clienteRepositorio;
	private final AsesorRepositorio asesorRepositorio;

	public RegistrarService() {
		this.clienteRepositorio = new ClienteRepositorio();
		this.asesorRepositorio = new AsesorRepositorio();
	}

	public Cliente registrarCliente(String identificacion, String nombre, String correo, String telefono,
								   String contrasena, Cliente.TipoCliente tipoCliente) {
		validarTexto(identificacion, "La identificación es requerida");
		validarTexto(nombre, "El nombre es requerido");
		validarTexto(correo, "El correo es requerido");
		validarTexto(telefono, "El teléfono es requerido");
		validarTexto(contrasena, "La contraseña es requerida");

		if (clienteRepositorio.obtenerCliente(correo) != null) {
			throw new IllegalArgumentException("Ya existe un cliente registrado con este correo");
		}

		if (tipoCliente == null) {
			tipoCliente = Cliente.TipoCliente.PERSONA_NATURAL;
		}

		Cliente cliente = Cliente.builder()
				.identificacion(identificacion)
				.nombre(nombre)
				.correo(correo)
				.telefono(telefono)
				.contrasena(contrasena)
				.tipoCliente(tipoCliente)
				.presupuesto(0)
				.zonasDeInteres(new ArrayList<>())
				.tipoInmuebleDeseado(Inmueble.TipoInmueble.CASA)
				.cantidadMinimaHabitaciones(1)
				.estadoBusqueda(Cliente.EstadoBusqueda.ACTIVA)
				.build();

		clienteRepositorio.crearCliente(cliente);
		return cliente;
	}

	public Cliente registrarClienteCompleto(String identificacion, String nombre, String correo, String telefono,
							String contrasena, Cliente.TipoCliente tipoCliente, double presupuesto,
							String zonasDeInteres, Inmueble.TipoInmueble tipoInmuebleDeseado,
							int cantidadMinimaHabitaciones, Cliente.EstadoBusqueda estadoBusqueda) {
		validarTexto(identificacion, "La identificación es requerida");
		validarTexto(nombre, "El nombre es requerido");
		validarTexto(correo, "El correo es requerido");
		validarTexto(telefono, "El teléfono es requerido");
		validarTexto(contrasena, "La contraseña es requerida");

		if (clienteRepositorio.obtenerCliente(correo) != null) {
			throw new IllegalArgumentException("Ya existe un cliente registrado con este correo");
		}

		List<String> zonas = new ArrayList<>();
		if (zonasDeInteres != null && !zonasDeInteres.trim().isEmpty()) {
			zonas = Arrays.stream(zonasDeInteres.split(","))
					.map(String::trim)
					.filter(z -> !z.isEmpty())
					.toList();
		}

		if (tipoCliente == null) {
			tipoCliente = Cliente.TipoCliente.PERSONA_NATURAL;
		}
		if (tipoInmuebleDeseado == null) {
			tipoInmuebleDeseado = Inmueble.TipoInmueble.CASA;
		}
		if (estadoBusqueda == null) {
			estadoBusqueda = Cliente.EstadoBusqueda.ACTIVA;
		}

		Cliente cliente = Cliente.builder()
				.identificacion(identificacion)
				.nombre(nombre)
				.correo(correo)
				.telefono(telefono)
				.contrasena(contrasena)
				.tipoCliente(tipoCliente)
				.presupuesto(presupuesto)
				.zonasDeInteres(zonas)
				.tipoInmuebleDeseado(tipoInmuebleDeseado)
				.cantidadMinimaHabitaciones(cantidadMinimaHabitaciones)
				.estadoBusqueda(estadoBusqueda)
				.build();

		clienteRepositorio.crearCliente(cliente);
		return cliente;
	}

	public Asesor registrarAsesor(String identificacion, String nombre, String correo, String telefono,
								 String contrasena, String especialidad, double comisionPorcentaje) {
		validarTexto(identificacion, "La identificación es requerida");
		validarTexto(nombre, "El nombre es requerido");
		validarTexto(correo, "El correo es requerido");
		validarTexto(telefono, "El teléfono es requerido");
		validarTexto(contrasena, "La contraseña es requerida");

		if (asesorRepositorio.obtenerAsesor(correo) != null) {
			throw new IllegalArgumentException("Ya existe un asesor registrado con este correo");
		}

		if (especialidad == null || especialidad.trim().isEmpty()) {
			especialidad = "General";
		}

		Asesor asesor = new Asesor(identificacion, nombre, correo, telefono, contrasena, especialidad, comisionPorcentaje);
		asesorRepositorio.crearAsesor(asesor);
		return asesor;
	}

	public void registrarDesdeFormulario(String nombre, String apellido, String correo, String telefono,
										String tipoUsuario, String contrasena) {
		validarTexto(nombre, "El nombre es requerido");
		validarTexto(apellido, "El apellido es requerido");
		validarTexto(correo, "El correo es requerido");
		validarTexto(telefono, "El teléfono es requerido");
		validarTexto(tipoUsuario, "El tipo de usuario es requerido");
		validarTexto(contrasena, "La contraseña es requerida");

		String identificacion = UUID.randomUUID().toString();
		String nombreCompleto = (nombre + " " + apellido).trim();
		String tipoNormalizado = tipoUsuario.trim().toLowerCase();

		if (tipoNormalizado.equals("asesor")) {
			registrarAsesor(identificacion, nombreCompleto, correo, telefono, contrasena, "General", 10.0);
			return;
		}

		Cliente.TipoCliente tipoCliente = Cliente.TipoCliente.PERSONA_NATURAL;
		if (tipoNormalizado.contains("empresa")) {
			tipoCliente = Cliente.TipoCliente.EMPRESA;
		} else if (tipoNormalizado.contains("inversionista")) {
			tipoCliente = Cliente.TipoCliente.INVERSIONISTA;
		}

		registrarCliente(identificacion, nombreCompleto, correo, telefono, contrasena, tipoCliente);
	}

	private void validarTexto(String valor, String mensaje) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new IllegalArgumentException(mensaje);
		}
	}
}
