package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Cliente;
import com.uniquindio.Model.Usuario;
import com.uniquindio.Service.LoginService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    
    ControladorPrincipal controladorPrincipal;
    LoginService loginService;

    public LoginController() {
        this.controladorPrincipal = ControladorPrincipal.getInstancia();
        this.loginService = new LoginService();
    }   

    /**
     * Redirige la ruta raíz al login
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    /**
     * Muestra la página de login (GET)
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Agregar atributos al modelo si es necesario
        model.addAttribute("titulo", "Iniciar Sesión");
        return "login"; // Retorna el archivo login.html
    }

    /**
     * Muestra la página de registro (GET)
     */
    @GetMapping("/registro")
    public String showRegisterPage(Model model) {
        model.addAttribute("titulo", "Registrarse");
        return "registro"; // Retorna el archivo registro.html
    }

    /**
     * Muestra la página de recuperación de contraseña (GET)
     */
    @GetMapping("/olvido-contrasena")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("titulo", "Olvidé mi contraseña");
        return "olvido-contrasena";
    }

    /**
     * Procesa la solicitud de recuperación de contraseña
     */
    @PostMapping("/olvido-contrasena")
    public String processForgotPassword(
            @RequestParam String email,
            Model model) {
        model.addAttribute("titulo", "Olvidé mi contraseña");
        model.addAttribute("message", "Si el correo existe, enviaremos las instrucciones de recuperación.");
        return "olvido-contrasena";
    }

    /**
     * Procesa el formulario de login (POST)
     * Recibe: email, password y opcional rememberMe
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) boolean rememberMe,
            HttpSession session,
            Model model) {
        
        // Validar que no estén vacíos
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            model.addAttribute("error", "El correo y contraseña son requeridos");
            return "login";
        }

        //  validar datos
        try {
            Usuario usuario = loginService.iniciarSesion(email, password);

            if (usuario != null) {
                if (usuario.getTipo() == Usuario.TipoUsuario.ASESOR) {
                    session.setAttribute("asesorSesion", (Asesor) usuario);
                    return "redirect:/home";
                }

                if (usuario.getTipo() == Usuario.TipoUsuario.CLIENTE) {
                    session.setAttribute("clienteSesion", (Cliente) usuario);
                    return "redirect:/home-cliente";
                }

                model.addAttribute("error", "El usuario autenticado no es asesor");
                return "login";
            } else {
                // Credenciales inválidas
                model.addAttribute("error", "Correo o contraseña incorrectos");
                model.addAttribute("email", email); // Mantener email en el formulario
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error en el sistema: " + e.getMessage());
            return "login";
        }
    }
}
