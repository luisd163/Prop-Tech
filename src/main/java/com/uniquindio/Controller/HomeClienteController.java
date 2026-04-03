package com.uniquindio.Controller;

import com.uniquindio.Model.Cliente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class HomeClienteController {

    @GetMapping("/home-cliente")
    public String showHomeCliente(
            @SessionAttribute(name = "clienteSesion", required = false) Cliente cliente,
            Model model) {
        if (cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("titulo", "Panel Cliente");
        model.addAttribute("cliente", cliente);
        model.addAttribute("nombreCliente", cliente.getNombre());
        model.addAttribute("saludo", "Bienvenido(a), " + cliente.getNombre());
        return "home-cliente";
    }
}
