package com.uniquindio.Model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.uniquindio.Repositorio.InmuebleRepositorio;

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
	private List<Inmueble> favoritos;
	private ArrayList<Inmueble> intenciones;
	private List<Inmueble> historialConsultas;
	private Inmueble inmuebleAsignado;

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
	public void registrarIntencion(
        Cliente cliente,
        Inmueble inmueble) {

    	// Si la lista no existe, crearla

    	if (cliente.getIntenciones() == null) {
        	cliente.setIntenciones(new ArrayList<>());
    	}

    	// Evitar duplicados

    	boolean existe = cliente.getIntenciones()
            .stream()
            .anyMatch(i ->
                    i.getCodigo()
                            .equals(inmueble.getCodigo())
            );

    	if (!existe) {
    	    cliente.getIntenciones().add(inmueble);
    	}
	}

	public List<Inmueble> consultarHistorial(
        Cliente cliente
	) {

    if (cliente.getHistorialConsultas() == null) {
        cliente.setHistorialConsultas(new ArrayList<>());
    }

    return cliente.getHistorialConsultas();
}

	public void registrarConsulta(Inmueble inmueble) {

		if (inmueble == null || inmueble.getCodigo() == null) {
			return;
		}

		String codigo = inmueble.getCodigo().trim();
		if (codigo.isEmpty()) {
			return;
		}

		// Intentar obtener la instancia canónica del repositorio
		InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
		Inmueble real = inmuebleRepositorio.obtenerInmueble(codigo);
		if (real == null) {
			real = inmuebleRepositorio.obtenerInmuebles().values().stream()
					.filter(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equalsIgnoreCase(codigo))
					.findFirst()
					.orElse(inmueble);
		}

		if (historialConsultas == null) {
			historialConsultas = new ArrayList<>();
		}

		// Eliminar cualquier entrada previa con el mismo código
		historialConsultas.removeIf(i -> i != null && i.getCodigo() != null && i.getCodigo().trim().equalsIgnoreCase(codigo));

		// Añadir al inicio para que lo más reciente aparezca primero
		historialConsultas.add(0, real);
	}
}
