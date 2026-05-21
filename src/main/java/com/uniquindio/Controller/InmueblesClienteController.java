package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.text.NumberFormat;
import java.util.stream.Collectors;

@Controller
public class InmueblesClienteController {

	@GetMapping("/inmuebles-cliente")
	public String showInmueblesCliente(
			@SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
			@RequestParam(name = "ciudad", required = false) String ciudad,
			@RequestParam(name = "tipo", required = false) String tipo,
			@RequestParam(name = "finalidad", required = false) String finalidad,
			@RequestParam(name = "habMin", required = false) String habMin,
			@RequestParam(name = "baMin", required = false) String baMin,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			Model model) {
		if (cliente == null) {
			return "redirect:/login";
		}

		String nombre = cliente.getNombre() != null ? cliente.getNombre().trim() : "";
		String iniciales = "CL";
		if (!nombre.isEmpty()) {
			String[] parts = nombre.split("\\s+");
			if (parts.length == 1) {
				iniciales = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
			} else {
				String p1 = parts[0].substring(0, 1);
				String p2 = parts[1].substring(0, 1);
				iniciales = (p1 + p2).toUpperCase();
			}
		}

		// Obtener tipos disponibles a partir del servicio de inmuebles
		List<String> tiposDisponibles = Arrays.stream(Inmueble.TipoInmueble.values())
				.map(Enum::name)
				.collect(Collectors.toList());
		List<String> finalidadesDisponibles = Arrays.stream(Inmueble.Finalidad.values())
				.map(Enum::name)
				.collect(Collectors.toList());
		List<String> habitacionesDisponibles = Arrays.asList("1", "2", "3", "4", "5", "+5");
		List<String> banosDisponibles = Arrays.asList("1", "2", "3", "4", "5", "+5");
		InmuebleService inmuebleService = new InmuebleService();
		List<Inmueble> itemsPrincipales = inmuebleService.obtenerTodosInmuebles();

		// Filtrar por ciudad si se envía parámetro
		if (ciudad != null && !ciudad.trim().isEmpty()) {
			String q = ciudad.trim().toLowerCase();
			itemsPrincipales = itemsPrincipales.stream()
					.filter(i -> i.getCiudad() != null && i.getCiudad().toLowerCase().contains(q))
					.collect(Collectors.toList());
		}

		// Filtrar por tipo
		if (tipo != null && !tipo.trim().isEmpty()) {
			String t = tipo.trim().toLowerCase();
			itemsPrincipales = itemsPrincipales.stream()
					.filter(i -> i.getTipoInmueble() != null && i.getTipoInmueble().name().toLowerCase().equals(t))
					.collect(Collectors.toList());
		}

		// Filtrar por finalidad
		if (finalidad != null && !finalidad.trim().isEmpty()) {
			String f = finalidad.trim().toLowerCase();
			itemsPrincipales = itemsPrincipales.stream()
					.filter(i -> i.getFinalidad() != null && i.getFinalidad().name().toLowerCase().equals(f))
					.collect(Collectors.toList());
		}

		// Filtrar por habitaciones minimas
		if (habMin != null && !habMin.trim().isEmpty()) {
			String hq = habMin.trim();
			if ("+5".equals(hq)) {
				itemsPrincipales = itemsPrincipales.stream()
						.filter(i -> i.getNumeroHabitaciones() >= 5)
						.collect(Collectors.toList());
			} else {
				try {
					int minH = Integer.parseInt(hq);
					itemsPrincipales = itemsPrincipales.stream()
							.filter(i -> i.getNumeroHabitaciones() >= minH)
							.collect(Collectors.toList());
				} catch (NumberFormatException ex) {
					// ignorar valor inválido
				}
			}
		}

		// Filtrar por baños minimos
		if (baMin != null && !baMin.trim().isEmpty()) {
			String bq = baMin.trim();
			if ("+5".equals(bq)) {
				itemsPrincipales = itemsPrincipales.stream()
						.filter(i -> i.getNumeroBanos() >= 5)
						.collect(Collectors.toList());
			} else {
				try {
					int minB = Integer.parseInt(bq);
					itemsPrincipales = itemsPrincipales.stream()
							.filter(i -> i.getNumeroBanos() >= minB)
							.collect(Collectors.toList());
				} catch (NumberFormatException ex) {
					// ignorar valor inválido
				}
			}
		}

		// Aplicar ordenamiento (de mayor a menor)
		if (sortBy != null && !sortBy.trim().isEmpty()) {
			String sort = sortBy.trim().toLowerCase();
			switch(sort) {
				case "precio":
					itemsPrincipales.sort((a, b) -> Float.compare(b.getPrecio(), a.getPrecio()));
					break;
				case "metros":
					itemsPrincipales.sort((a, b) -> Double.compare(b.getArea(), a.getArea()));
					break;
				case "banos":
					itemsPrincipales.sort((a, b) -> Integer.compare(b.getNumeroBanos(), a.getNumeroBanos()));
					break;
				case "habitaciones":
					itemsPrincipales.sort((a, b) -> Integer.compare(b.getNumeroHabitaciones(), a.getNumeroHabitaciones()));
					break;
			}
		}

		int totalInmuebles = itemsPrincipales.size();

		model.addAttribute("titulo", "Inmuebles disponibles");
		model.addAttribute("cliente", cliente);
		model.addAttribute("nombreCliente", cliente.getNombre());
		model.addAttribute("inicialesCliente", iniciales);
		model.addAttribute("rolCliente", "Cliente activa");
		model.addAttribute("totalInmuebles", totalInmuebles);
		model.addAttribute("searchCiudad", ciudad != null ? ciudad : "");
		model.addAttribute("selectedTipo", tipo != null ? tipo : "");
		model.addAttribute("selectedFinalidad", finalidad != null ? finalidad : "");
		model.addAttribute("selectedHabMin", habMin != null ? habMin : "");
		model.addAttribute("selectedBaMin", baMin != null ? baMin : "");
		model.addAttribute("selectedSortBy", sortBy != null ? sortBy : "");
		model.addAttribute("itemsPrincipales", itemsPrincipales);
		model.addAttribute("currencyFormatter", NumberFormat.getCurrencyInstance(Locale.of("es", "CO")));
		model.addAttribute("tiposDisponibles", tiposDisponibles);
		model.addAttribute("finalidadesDisponibles", finalidadesDisponibles);
		model.addAttribute("habitacionesDisponibles", habitacionesDisponibles);
		model.addAttribute("banosDisponibles", banosDisponibles);

		return "inmuebles-cliente";
	}
}
