package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Service.AsesorHomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

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

        model.addAttribute("titulo", "Panel Asesor");
        model.addAttribute("asesor", asesor);
        model.addAttribute("nombreAsesor", asesor.getNombre());
        model.addAttribute("saludo", "Bienvenido(a), " + asesor.getNombre());
        model.addAttribute("alertasTotalesTexto", totalAlertas + " alertas");
        return "home-asesor";
    }

}
