package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class InmueblesClienteController {

	@GetMapping("/inmuebles-cliente")
	public String showInmueblesCliente(
			@SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
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
		int totalInmuebles = new InmuebleService().obtenerTodosInmuebles().size();

		model.addAttribute("titulo", "Inmuebles disponibles");
		model.addAttribute("cliente", cliente);
		model.addAttribute("nombreCliente", cliente.getNombre());
		model.addAttribute("inicialesCliente", iniciales);
		model.addAttribute("rolCliente", "Cliente activa");
		model.addAttribute("totalInmuebles", totalInmuebles);
		model.addAttribute("tiposDisponibles", tiposDisponibles);
		model.addAttribute("finalidadesDisponibles", finalidadesDisponibles);
		model.addAttribute("habitacionesDisponibles", habitacionesDisponibles);
		model.addAttribute("banosDisponibles", banosDisponibles);

		return "inmuebles-cliente";
	}
}
