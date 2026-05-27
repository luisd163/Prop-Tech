package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Repositorio.AsesorRepositorio;
import com.uniquindio.Repositorio.ClienteRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
public class ReportesAsesorController {

    @GetMapping("/reportes-asesor")
    public String showReportesAsesor(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        List<Cliente> clientes = asesor.getClientes() == null ? new ArrayList<>() : asesor.getClientes();
        List<Cliente> clientesConInmuebleAsignado = clientes.stream()
                .filter(cliente -> cliente != null && cliente.getInmuebleAsignado() != null)
                .toList();

        List<Inmueble> inmueblesAsignados = clientesConInmuebleAsignado.stream()
                .map(Cliente::getInmuebleAsignado)
                .filter(inmueble -> inmueble != null)
                .toList();

        model.addAttribute("titulo", "Reportes");
        model.addAttribute("inmueblesAsignados", inmueblesAsignados);
        model.addAttribute("clientesConInmuebleAsignado", clientesConInmuebleAsignado);
        model.addAttribute("kpiTotalMes", inmueblesAsignados.size());
        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("rolAsesor", "Asesor inmobiliario");
        model.addAttribute("inicialesAsesor", obtenerIniciales(asesor.getNombre()));

        return "reportes-asesor";
    }

    @PostMapping("/reportes-asesor/cancelar-contrato")
    public String cancelarContrato(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            HttpSession session,
            @RequestParam(name = "clienteId") String clienteId
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        if (clienteId == null || clienteId.isBlank()) {
            return "redirect:/reportes-asesor";
        }

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        Cliente clientePersistido = clienteRepositorio.obtenerClientes().get(clienteId);

        if (clientePersistido == null) {
            return "redirect:/reportes-asesor";
        }

        clientePersistido.setInmuebleAsignado(null);
        clienteRepositorio.crearCliente(clientePersistido);

        if (asesor.getClientes() != null) {
            for (Cliente cliente : asesor.getClientes()) {
                if (cliente != null && clienteId.equals(cliente.getIdentificacion())) {
                    cliente.setInmuebleAsignado(null);
                    break;
                }
            }
        }

        AsesorRepositorio asesorRepositorio = new AsesorRepositorio();
        asesorRepositorio.crearAsesor(asesor);
        session.setAttribute("asesorSesion", asesor);

        return "redirect:/reportes-asesor";
    }

    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "--";
        }

        String[] partes = nombre.trim().split("\\s+");
        StringBuilder iniciales = new StringBuilder();

        for (String parte : partes) {
            if (!parte.isBlank()) {
                iniciales.append(Character.toUpperCase(parte.charAt(0)));
            }
            if (iniciales.length() == 2) {
                break;
            }
        }

        return iniciales.length() == 0 ? "--" : iniciales.toString();
    }
}
