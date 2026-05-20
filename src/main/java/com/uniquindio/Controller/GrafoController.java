package com.uniquindio.Controller;

import com.uniquindio.Estructuras.GrafoInmuebles;
import com.uniquindio.Model.Inmueble;
import com.uniquindio.Repositorio.InmuebleRepositorio;
import com.uniquindio.Service.GrafoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class GrafoController {

    @Autowired
    private GrafoService grafoService;
    @Autowired
    private InmuebleRepositorio inmuebleRepositorio;

    @GetMapping("/grafo/{ciudad}")
    public String mostrarGrafo(
            @PathVariable String ciudad,
            Model model
    ) {

        // SIMULACIÓN
        // Aquí luego reemplazas con tu lista real desde JSON

        HashMap<String, Inmueble> inmuebles = inmuebleRepositorio.obtenerInmuebles();

        GrafoInmuebles grafo = grafoService.construirGrafo(inmuebles, ciudad);

        model.addAttribute("ciudad", ciudad);
        model.addAttribute("grafo", grafo.mostrarGrafo());

        return "grafo";
    }

    @GetMapping("/grafo")
    public String showGrafoView(Model model) {
        HashMap<String, Inmueble> inmuebles = inmuebleRepositorio.obtenerInmuebles();
        GrafoInmuebles grafo = grafoService.construirGrafo(inmuebles, "todas");
        
        model.addAttribute("ciudad", "Todas las ciudades");
        model.addAttribute("grafo", grafo.mostrarGrafo());
        
        return "grafo";
    }
    
}