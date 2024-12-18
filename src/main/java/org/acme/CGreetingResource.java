package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.nio.file.*;
import org.glassfish.jersey.media.multipart.FormDataParam;
import jakarta.ws.rs.Path;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Path("/analizar-contrato")
public class CGreetingResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response analyzeContract(@FormDataParam("pdf") InputStream uploadedInputStream) throws Exception {
        // Guardar el archivo temporalmente
        String tempFilePath = "src/temp/tempContrato.pdf";
        File tempFile = new File(tempFilePath);
        Files.copy(uploadedInputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // Usar REPLACE_EXISTING para reemplazar archivo si ya existe

        // Procesar el PDF con la lógica de pdf_form_filler
        boolean isFinished = PdfFormFiller.loadPdfData(tempFilePath);

        if (!isFinished) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Error al procesar el contrato").build();
        }
        
        String PDFtext = PdfFormFiller.extractTextFromPdf(tempFilePath);
        String[] clausulas = PdfFormFiller.getClausesArrayFromText(PDFtext);
        String JSONAmandar = JsonGenerator.generateJson(clausulas);
        
        String responseServer = HttpClientUtil.sendJsonToEvaluate(JSONAmandar);

        Gson gson = new Gson();
        // Definir el tipo para mapear el JSON como una lista de Map<String, Object>
        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();

        // Convertir el JSON en una lista de mapas
        List<Map<String, Object>> clauses = gson.fromJson(responseServer, listType);

        // Lista para almacenar las cláusulas violadas
        List<Map<String, Object>> violatedClauses = new ArrayList<>();
        
        boolean isViolation = false;
        // Recorrer la lista y procesar cada entrada
        for (Map<String, Object> clause : clauses) {
            boolean violation = (boolean) clause.get("violation");
            if(violation){
                isViolation = true;
                violatedClauses.add(clause); // Almacenar la cláusula violada
            }
        }

        if (isViolation) {
            // Lista para almacenar los artículos y sus leyes correspondientes
            List<Map<String, String>> articlesWithLaws = new ArrayList<>();

            // Set para almacenar los artículos que ya han sido procesados
            Set<String> processedArticles = new HashSet<>();

            //Cargar csv
            LeyesExtractor leyesExtractor = new LeyesExtractor("src/assets/csv_leyes.csv");
            for (Map<String, Object> violatedClause : violatedClauses) {
                String article = (String) violatedClause.get("article"); // Convertir el Object a String
                if(!processedArticles.contains(article)){
                    String ley = leyesExtractor.obtenerTextoLey(article);
                    Map<String, String> articleWithLaw = Map.of("article", article, "ley", ley);
                    articlesWithLaws.add(articleWithLaw);
                    processedArticles.add(article);
                }
            }
            
            // Cargar la plantilla y rellenarla con los datos
            String templateText = PdfGenerator.loadTemplate();
            String content = PdfGenerator.fillTemplate(templateText, PdfFormFiller.sellerDict);
            content = PdfGenerator.fillTemplate(content, PdfFormFiller.buyerDict);
            content = PdfGenerator.fillArticles(content, articlesWithLaws);

            // Crear el PDF de salida
            String outputFilePath = "src/tmp/tempContrato.pdf";
            PdfGenerator.createPdf(content, outputFilePath);

            // Devolver el archivo generado como respuesta
            File outputFile = new File(outputFilePath);
            
            return Response.ok(outputFile)
            .type("application/pdf")  // Establecer explícitamente el tipo de contenido
            .header("Content-Disposition", "attachment; filename=DemandaGenerado.pdf")
            .build();

        } else {
            String messageOk = "No se han encontrado ninguna violación en las cláusulas.";
            return Response.ok(messageOk)
            .type("text/plain")  // Establecer explícitamente el tipo de contenido
            .build();
        }
    }
}
