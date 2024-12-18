package org.acme;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeyesExtractor {
// Mapa para almacenar los artículos y sus textos desde el CSV
    private Map<String, String> leyesMap;

    public LeyesExtractor(String csvFile) {
        this.leyesMap = new HashMap<>();
        cargarLeyesDesdeCSV(csvFile);
    }

    // Método para cargar los datos del CSV en un mapa
    private void cargarLeyesDesdeCSV(String csvFile) {
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] row;
            // Saltar la primera línea (encabezado)
            csvReader.readNext();

            // Leer el archivo CSV y almacenarlo en el mapa
            while ((row = csvReader.readNext()) != null) {
                String article = row[0].trim();
                String text = row[1].trim();
                leyesMap.put(article, text);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener el texto de la ley correspondiente al artículo
    public String obtenerTextoLey(String article) {
        return leyesMap.getOrDefault(article, "Texto no encontrado para el artículo: " + article);
    }

    // Método que devuelve los textos de las leyes violadas a partir de un listado de cláusulas
    public List<String> obtenerTextosLeyesVioladas(List<Map<String, Object>> violatedClauses) {
        List<String> violatedTexts = new ArrayList<>();

        // Recorrer las cláusulas violadas y buscar los textos en el mapa
        for (Map<String, Object> clause : violatedClauses) {
            String article = (String) clause.get("article"); // Obtener el artículo
            String lawText = obtenerTextoLey(article);      // Obtener el texto de la ley

            violatedTexts.add(lawText); // Almacenar el texto correspondiente
        }
        return violatedTexts;
    }
    
}
