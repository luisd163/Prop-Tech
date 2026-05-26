package com.uniquindio.Persistencia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.uniquindio.Model.Cliente;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class ClientePersistencia {

    private static final String ARCHIVO = "data/clientes.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void guardar(HashMap<String, Cliente> clientes) {
        try {
            File carpeta = new File("data");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            FileWriter writer = new FileWriter(ARCHIVO);
            gson.toJson(clientes, writer);
            writer.flush();
            writer.close();
            System.out.println("Clientes guardados correctamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar clientes: " + e.getMessage());
        }
    }

    public static HashMap<String, Cliente> cargar() {
        try {
            File archivo = new File(ARCHIVO);
            if (!archivo.exists()) {
                System.out.println("No se encontró archivo de clientes. Se inicia vacío.");
                return new HashMap<>();
            }

            FileReader reader = new FileReader(archivo);
            Type tipo = new TypeToken<HashMap<String, Cliente>>() {}.getType();
            HashMap<String, Cliente> datos = gson.fromJson(reader, tipo);
            reader.close();

            if (datos == null) {
                return new HashMap<>();
            }

            System.out.println("✔ " + datos.size() + " clientes cargados.");
            return datos;
        } catch (IOException e) {
            System.out.println("Error al cargar clientes: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
