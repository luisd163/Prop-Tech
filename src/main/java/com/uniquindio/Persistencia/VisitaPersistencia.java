package com.uniquindio.Persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.uniquindio.Model.Visita;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;

public class VisitaPersistencia {

    private static final String ARCHIVO = "data/visitas.json";

    private static final JsonSerializer<LocalDate> LOCAL_DATE_SERIALIZER =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    private static final JsonDeserializer<LocalDate> LOCAL_DATE_DESERIALIZER =
            (json, typeOfT, context) -> LocalDate.parse(json.getAsString());

    private static final JsonSerializer<LocalTime> LOCAL_TIME_SERIALIZER =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString());
    private static final JsonDeserializer<LocalTime> LOCAL_TIME_DESERIALIZER =
            (json, typeOfT, context) -> LocalTime.parse(json.getAsString());

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_SERIALIZER)
            .registerTypeAdapter(LocalDate.class, LOCAL_DATE_DESERIALIZER)
            .registerTypeAdapter(LocalTime.class, LOCAL_TIME_SERIALIZER)
            .registerTypeAdapter(LocalTime.class, LOCAL_TIME_DESERIALIZER)
            .setPrettyPrinting()
            .create();

    public static void guardar(HashMap<String, Visita> visitas) {
        try {
            File carpeta = new File("data");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            String json = gson.toJson(visitas);
            Files.writeString(Path.of(ARCHIVO), json);
            System.out.println("Visitas guardadas correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar visitas: " + e.getMessage());
        }
    }

    public static HashMap<String, Visita> cargar() {
        try {
            File archivo = new File(ARCHIVO);
            if (!archivo.exists()) {
                System.out.println("No se encontró archivo de visitas. Se inicia vacío.");
                return new HashMap<>();
            }

            java.io.FileReader reader = new java.io.FileReader(archivo);
            Type tipo = new TypeToken<HashMap<String, Visita>>() {}.getType();
            HashMap<String, Visita> datos = gson.fromJson(reader, tipo);
            reader.close();

            if (datos == null) {
                return new HashMap<>();
            }

            System.out.println("✔ " + datos.size() + " visitas cargadas.");
            return datos;
        } catch (IOException e) {
            System.out.println("Error al cargar visitas: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
