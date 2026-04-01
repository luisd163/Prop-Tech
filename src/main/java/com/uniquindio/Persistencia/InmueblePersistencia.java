package com.uniquindio.Persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uniquindio.Model.Inmueble;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;

public class InmueblePersistencia {
    private static final String ARCHIVO = "data/inmuebles.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void guardar(HashMap<String, Inmueble> inmuebles) {
        try {
            File carpeta = new File("data");
            if (!carpeta.exists()) carpeta.mkdirs();

            FileWriter writer = new FileWriter(ARCHIVO);
            gson.toJson(inmuebles, writer);
            writer.flush();
            writer.close();
            System.out.println("Inmuebles guardados correctamente.");

        } catch (IOException e) {
            System.out.println("Error al guardar inmuebles: " + e.getMessage());
        }
    }

    public static HashMap<String, Inmueble> cargar() {
        try {
            File archivo = new File(ARCHIVO);
            if (!archivo.exists()) {
                System.out.println("No se encontró archivo de inmuebles. Se inicia vacío.");
                return new HashMap<>();
            }

            FileReader reader = new FileReader(archivo);
            Type tipo = new TypeToken<HashMap<String, Inmueble>>(){}.getType();
            HashMap<String, Inmueble> datos = gson.fromJson(reader, tipo);
            reader.close();

            if (datos == null) return new HashMap<>();
            System.out.println("✔ " + datos.size() + "inmuebles cargados.");
            return datos;

        } catch (IOException e) {
            System.out.println("Error al cargar inmuebles: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
