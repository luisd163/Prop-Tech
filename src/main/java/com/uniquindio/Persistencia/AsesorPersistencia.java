package com.uniquindio.Persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uniquindio.Model.Asesor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Type;
import java.util.HashMap;

public class AsesorPersistencia {

    private static final String ARCHIVO = "data/asesores.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void guardar(HashMap<String, Asesor> asesores) {
        try {
            File carpeta = new File("data");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            String json = gson.toJson(asesores);
            Files.writeString(Path.of(ARCHIVO), json);
            System.out.println("Asesores guardados correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar asesores: " + e.getMessage());
        }
    }

    public static HashMap<String, Asesor> cargar() {
        try {
            File archivo = new File(ARCHIVO);
            if (!archivo.exists()) {
                System.out.println("No se encontró archivo de asesores. Se inicia vacío.");
                return new HashMap<>();
            }

            java.io.FileReader reader = new java.io.FileReader(archivo);
            Type tipo = new TypeToken<HashMap<String, Asesor>>() {}.getType();
            HashMap<String, Asesor> datos = gson.fromJson(reader, tipo);
            reader.close();

            if (datos == null) {
                return new HashMap<>();
            }

            System.out.println("✔ " + datos.size() + " asesores cargados.");
            return datos;
        } catch (IOException e) {
            System.out.println("Error al cargar asesores: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
