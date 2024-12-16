package org.acme;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientUtil {

    public static String sendPostRequest(String url, String json) throws Exception {
        // Crear cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crear solicitud HTTP
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        // Enviar la solicitud y obtener la respuesta
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Devolver la respuesta como String
        return response.body();
    }
    public static void sendJsonToEvaluate(String json) {
        try {

            // URL del endpoint
            String url = "http://127.0.0.1:5320/evaluate";

            // Enviar la solicitud POST
            String response = HttpClientUtil.sendPostRequest(url, json);

            // Imprimir la respuesta
            System.out.println("Respuesta del servidor: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
