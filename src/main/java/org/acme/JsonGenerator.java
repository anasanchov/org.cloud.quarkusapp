package org.acme;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonGenerator {

    public static String generateJson(String[] clauses) {
        // Crear el objeto JSON principal
        JsonObject jsonObject = new JsonObject();

        // Crear el array JSON para las cláusulas
        JsonArray clausesArray = new JsonArray();

        // Añadir cada cláusula al array
        for (String clause : clauses) {
            clausesArray.add(clause);
        }

        // Añadir el array de cláusulas al objeto JSON principal
        jsonObject.add("clauses", clausesArray);

        // Convertir el objeto JSON a String
        return jsonObject.toString();
    }

    public static void main(String[] args) {
        // Array de cláusulas
        String[] clauses = {
            "El comprador no tiene derecho a garantía.",
            "El vendedor puede rescindir el contrato sin causa justificada.",
            "Ambas partes deben cumplir con las condiciones pactadas."
        };

        // Generar el JSON
        String json = generateJson(clauses);

        // Mostrar el JSON
        System.out.println(json);
    }
}

