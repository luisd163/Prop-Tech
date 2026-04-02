package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Service.AsesorHomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
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
        List<Inmueble> inmueblesAsesor = asesorHomeService.obtenerInmueblesAsesor(asesor);
        List<Inmueble> inmueblesLimitados = inmueblesAsesor.stream().limit(2).collect(Collectors.toList());

        model.addAttribute("titulo", "Panel Asesor");
        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("saludo", "Bienvenido(a), " + asesor.getNombre());
        model.addAttribute("alertasTotalesTexto", totalAlertas + " alertas");
        model.addAttribute("kpiInmueblesAsignados", cantidadInmuebles);
        model.addAttribute("inmueblesLimitados", inmueblesLimitados);
        return "home-asesor";
    }

}
