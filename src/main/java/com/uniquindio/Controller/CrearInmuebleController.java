package com.uniquindio.Controller;

import com.uniquindio.Model.Asesor;
import com.uniquindio.Model.Inmueble.Disponibilidad;
import com.uniquindio.Model.Inmueble.EstadoInmueble;
import com.uniquindio.Model.Inmueble.Finalidad;
import com.uniquindio.Model.Inmueble.TipoInmueble;
import com.uniquindio.Service.InmuebleService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CrearInmuebleController {

    private final InmuebleService inmuebleService;

    public CrearInmuebleController() {
        this.inmuebleService = new InmuebleService();
    }

    @GetMapping("/inmuebles/crear")
    public String showCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            Model model) {
        if (asesor == null) {
            return "redirect:/login";
        }
        cargarCombos(model);
        model.addAttribute("titulo", "Crear Inmueble");
        return "crear-inmueble";
    }

    @PostMapping("/inmuebles/crear")
    public String processCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor,
            @RequestParam String nombre,
            @RequestParam String direccion,
            @RequestParam String ciudad,
            @RequestParam String barrio,
            @RequestParam TipoInmueble tipoInmueble,
            @RequestParam Finalidad finalidad,
            @RequestParam String precio,
            @RequestParam double area,
            @RequestParam int numeroHabitaciones,
            @RequestParam int numeroBanos,
            @RequestParam EstadoInmueble estadoInmueble,
                @RequestParam(required = false) MultipartFile fotoPortada,
                @RequestParam(required = false) MultipartFile[] fotosGaleria,
            @RequestParam Disponibilidad disponibilidad,
            Model model
    ) {
        if (asesor == null) {
            return "redirect:/login";
        }

        try {
            String codigo = inmuebleService.generarCodigoUnico4Digitos();
            float precioParseado = parsearPrecio(precio);
            String rutaFotoPortada = guardarArchivoImagen(fotoPortada, "portada");
            List<String> rutasGaleria = guardarArchivosImagenes(fotosGaleria, "galeria");

            inmuebleService.registrarInmueble(
                    codigo,
                    nombre,
                    direccion,
                    ciudad,
                    barrio,
                    asesor.getIdentificacion(),
                    tipoInmueble,
                    finalidad,
                    precioParseado,
                    area,
                    numeroHabitaciones,
                    numeroBanos,
                    estadoInmueble,
                        disponibilidad,
                        rutaFotoPortada,
                        rutasGaleria
            );
            model.addAttribute("mensaje", "Inmueble registrado exitosamente.");
            cargarCombos(model);
            model.addAttribute("titulo", "Crear Inmueble");
            return "crear-inmueble";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("direccion", direccion);
            model.addAttribute("ciudad", ciudad);
            model.addAttribute("barrio", barrio);
            model.addAttribute("precio", precio);
            model.addAttribute("area", area);
            model.addAttribute("numeroHabitaciones", numeroHabitaciones);
            model.addAttribute("numeroBanos", numeroBanos);
            cargarCombos(model);
            model.addAttribute("titulo", "Crear Inmueble");
            return "crear-inmueble";
        }
    }

    private String guardarArchivoImagen(MultipartFile archivo, String prefijo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarImagen(archivo);

        try {
            String nombreOriginal = Paths.get(archivo.getOriginalFilename()).getFileName().toString();
            String extension = "";
            int ultimoPunto = nombreOriginal.lastIndexOf('.');
            if (ultimoPunto >= 0) {
                extension = nombreOriginal.substring(ultimoPunto);
            }

            String nombreUnico = prefijo + "_" + UUID.randomUUID() + extension;
            Path carpeta = Paths.get(System.getProperty("user.dir"), "uploads", "inmuebles");
            Files.createDirectories(carpeta);

            Path destino = carpeta.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/inmuebles/" + nombreUnico;
        } catch (IOException e) {
            throw new IllegalArgumentException("No fue posible guardar una de las imágenes");
        }
    }

    private List<String> guardarArchivosImagenes(MultipartFile[] archivos, String prefijo) {
        List<String> rutas = new ArrayList<>();
        if (archivos == null || archivos.length == 0) {
            return rutas;
        }

        for (MultipartFile archivo : archivos) {
            String ruta = guardarArchivoImagen(archivo, prefijo);
            if (ruta != null) {
                rutas.add(ruta);
            }
        }
        return rutas;
    }

    private void validarImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }
    }

    private float parsearPrecio(String precioTexto) {
        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es requerido");
        }

        String valor = precioTexto.trim().replace(" ", "");

        if (valor.contains(",")) {
            valor = valor.replace(".", "");
            valor = valor.replace(",", ".");
        } else {
            int ultimoPunto = valor.lastIndexOf('.');
            if (ultimoPunto > 0) {
                String parteEntera = valor.substring(0, ultimoPunto).replace(".", "");
                String parteDecimal = valor.substring(ultimoPunto + 1);
                valor = parteEntera + "." + parteDecimal;
            } else {
                valor = valor.replace(".", "");
            }
        }

        try {
            return Float.parseFloat(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de precio inválido");
        }
    }

    private void cargarCombos(Model model) {
        model.addAttribute("tiposInmueble", TipoInmueble.values());
        model.addAttribute("finalidades", Finalidad.values());
        model.addAttribute("estadosInmueble", EstadoInmueble.values());
        model.addAttribute("disponibilidades", Disponibilidad.values());
    }

    @GetMapping("/inmuebles/cancelar")
    public String cancelarCrearInmueble(
            @SessionAttribute(name = "asesorSesion", required = false) Asesor asesor) {
        if (asesor == null) {
            return "redirect:/login";
        }
        return "redirect:/home";
    }
}