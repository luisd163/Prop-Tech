package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Repositorio.AsesorRepositorio;
import com.uniquindio.Repositorio.ClienteRepositorio;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Service.AsesorHomeService;
import com.uniquindio.Service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class CrearVisitaController {

	@PostMapping("/visitas/crear")
	public String crearVisita(
			@SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
			@RequestParam String inmuebleCodigo,
			@RequestParam String clienteId,
			@RequestParam LocalDate fecha,
			@RequestParam String hora,
			@RequestParam(required = false) String observaciones,
			@RequestParam(required = false) Visita.EstadoVisita estado,
			Model model
	) {
		if (asesor == null) {
			return "redirect:/login";
		}

		InmuebleRepositorio inmuebleRepositorio = new InmuebleRepositorio();
		ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
		VisitaService visitaService = new VisitaService();

		Inmueble inmueble = inmuebleRepositorio.obtenerInmueble(inmuebleCodigo);
		Cliente cliente = clienteRepositorio.obtenerClientes().get(clienteId);

		try {
			if (inmueble == null) {
				throw new IllegalArgumentException("El código del inmueble no existe");
			}

			if (!asesor.getIdentificacion().equals(inmueble.getCodigoAsesorResponsable())) {
				throw new IllegalArgumentException("El inmueble no está asignado a tu asesoría");
			}

			if (cliente == null) {
				throw new IllegalArgumentException("El código del cliente no existe");
			}

			LocalTime horaParseada = parsearHora(hora);

			visitaService.crearVisita(
					null,
					cliente,
					inmueble,
					fecha,
					horaParseada,
					asesor.getIdentificacion(),
					estado,
					observaciones
			);

			if (asesor.getClientes() == null) {
				asesor.setClientes(new ArrayList<>());
			}

			boolean clienteYaAsociado = asesor.getClientes().stream()
					.anyMatch(c -> c != null && c.getIdentificacion() != null
							&& c.getIdentificacion().equals(cliente.getIdentificacion()));

			if (!clienteYaAsociado) {
				asesor.getClientes().add(cliente);
				AsesorRepositorio asesorRepositorio = new AsesorRepositorio();
				asesorRepositorio.crearAsesor(asesor);
			}

			return "redirect:/visitas";
		} catch (IllegalArgumentException e) {
			AsesorHomeService asesorHomeService = new AsesorHomeService();
			List<Inmueble> inmuebles = asesorHomeService.obtenerInmueblesAsesor(asesor);
			List<Cliente> clientes = clienteRepositorio.obtenerClientes().values().stream().collect(Collectors.toList());

			model.addAttribute("asesor", asesor);
			model.addAttribute("inmuebles", inmuebles);
			model.addAttribute("clientes", clientes);
			model.addAttribute("error", e.getMessage());

			model.addAttribute("inmuebleCodigo", inmuebleCodigo);
			model.addAttribute("clienteId", clienteId);
			model.addAttribute("fecha", fecha);
			model.addAttribute("hora", hora);
			model.addAttribute("observaciones", observaciones);

			return "crear-visita";
		}
	}

	private LocalTime parsearHora(String horaTexto) {
		if (horaTexto == null || horaTexto.isBlank()) {
			throw new IllegalArgumentException("La hora es obligatoria");
		}

		String horaNormalizada = horaTexto.trim().toLowerCase(Locale.ROOT)
				.replace("a. m.", "AM")
				.replace("a.m.", "AM")
				.replace("am", "AM")
				.replace("p. m.", "PM")
				.replace("p.m.", "PM")
				.replace("pm", "PM")
				.replace(" ", "");

		try {
			return LocalTime.parse(horaNormalizada, DateTimeFormatter.ofPattern("hh:mma", Locale.ROOT));
		} catch (DateTimeParseException ignored) {
		}

		try {
			return LocalTime.parse(horaTexto.trim(), DateTimeFormatter.ofPattern("H:mm", Locale.ROOT));
		} catch (DateTimeParseException ignored) {
		}

		throw new IllegalArgumentException("Formato de hora inválido. Usa por ejemplo 10:00 a.m. o 14:00");
	}
}
