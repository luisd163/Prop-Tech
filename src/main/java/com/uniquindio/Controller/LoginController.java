package com.uniquindio.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    
    ControladorPrincipal controladorPrincipal;

    public LoginController() {
        this.controladorPrincipal = ControladorPrincipal.getInstancia();
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
     * Procesa el formulario de login (POST)
     * Recibe: email, password y opcional rememberMe
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) boolean rememberMe,
            Model model) {
        
        // Validar que no estén vacíos
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            model.addAttribute("error", "El correo y contraseña son requeridos");
            return "login";
        }

        //  validar datos
        try {
            // Simular validación (reemplazar con lógica real)
            if (validarCredenciales(email, password)) {
                // Usuario válido, redirigir al dashboard
                return "redirect:/home";
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

    /**
     * Método para validar credenciales (reemplazar con lógica real)
     */
    private boolean validarCredenciales(String email, String password) {
        // REEMPLAZAR CON LÓGICA REAL (consultar BD, comparar contraseña)
        // Por ahora: solo validar formato de email
        return email.contains("@") && password.length() >= 6;
    }
}
