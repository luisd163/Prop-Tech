package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Model.Visita;
import com.uniquindio.Service.AsesorHomeService;
import com.uniquindio.Service.VisitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeAsesorController {
    
    ControladorPrincipal controladorPrincipal;
    AsesorHomeService asesorHomeService;

    public HomeAsesorController(){
        this.controladorPrincipal = ControladorPrincipal.getInstancia();
        this.asesorHomeService = new AsesorHomeService();
    }

    @GetMapping("/home")
    public String showAsesorHome(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            Model model) {
        if (asesor == null) {
            return "redirect:/login";
        }

        int totalAlertas = asesorHomeService.cantidadAlertas(asesor);
        int cantidadInmuebles = asesorHomeService.cantidadInmueblesAsociados(asesor);
        int cierresMes = asesorHomeService.cantidadCierresMes(asesor);
        List<Inmueble> inmueblesAsesor = asesorHomeService.obtenerInmueblesAsesor(asesor);
        List<Inmueble> inmueblesLimitados = inmueblesAsesor.stream().limit(2).collect(Collectors.toList());
        List<Cliente> clientesActivos = (asesor.getClientes() == null ? List.<Cliente>of() : asesor.getClientes())
            .stream()
            .filter(c -> c != null)
            .limit(2)
            .collect(Collectors.toList());

        VisitaService visitaService = new VisitaService();
        List<Visita> visitasAsesor = visitaService.obtenerVisitasPorAsesor(asesor.getIdentificacion());
        int visitasSemana = visitaService.filtrarEstaSemana(visitasAsesor).size();
        List<Visita> visitasProximas = visitasAsesor.stream()
            .filter(v -> v != null && v.getFecha() != null && v.getHora() != null)
            .filter(v -> !v.getFecha().isBefore(LocalDate.now()))
            .sorted(Comparator
                .comparing(Visita::getFecha)
                .thenComparing(Visita::getHora))
            .limit(2)
            .collect(Collectors.toList());

        String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));

        model.addAttribute("titulo", "Panel Asesor");
        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("saludo", "Bienvenido(a), " + asesor.getNombre());
        model.addAttribute("fechaActual", fechaActual);
        model.addAttribute("alertasTotalesTexto", totalAlertas + " alertas");
        model.addAttribute("kpiInmueblesAsignados", cantidadInmuebles);
        model.addAttribute("kpiVisitasSemana", visitasSemana);
        model.addAttribute("kpiCierresMes", cierresMes);
        model.addAttribute("inmueblesLimitados", inmueblesLimitados);
        model.addAttribute("visitasProximas", visitasProximas);
        model.addAttribute("clientesActivos", clientesActivos);
        return "home-asesor";
    }

}
