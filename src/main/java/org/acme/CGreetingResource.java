package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.nio.file.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.glassfish.jersey.media.multipart.FormDataParam;
import jakarta.ws.rs.Path;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


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
        PdfFormFiller pdfFormFiller = new PdfFormFiller();
        boolean isFinished = pdfFormFiller.loadPdfData(tempFilePath);
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
        
        boolean isViolation = false;
        // Recorrer la lista y procesar cada entrada
        for (Map<String, Object> clause : clauses) {
            //String clauseText = (String) clause.get("clause");
            boolean violation = (boolean) clause.get("violation");
            //System.out.println("Clause: " + clauseText);
            //System.out.println("Violation: " + violation);

            if(violation){
                isViolation = true;
                break;
            }
        }

        if (isViolation) {
            //System.out.println("¡Hay al menos una violación en las cláusulas!");
            // Cargar la plantilla y rellenarla con los datos
            System.out.println("CLAUSULAS: "+ clauses);
            PdfGenerator pdfGenerator = new PdfGenerator();
            String templateText = pdfGenerator.loadTemplate();
            System.out.println("TEMPLATE: "+templateText);
            String content = pdfGenerator.fillTemplate(templateText, pdfFormFiller.sellerDict);
            content = pdfGenerator.fillTemplate(content, pdfFormFiller.buyerDict);

            // Crear el PDF de salida
            String outputFilePath = "src/tmp/tempContrato.pdf";
            pdfGenerator.createPdf(content);

            // Devolver el archivo generado como respuesta
            File outputFile = new File(outputFilePath);

            return Response.ok(outputFile)
            .type("application/pdf")  // Establecer explícitamente el tipo de contenido
            .header("Content-Disposition", "attachment; filename=ContratoGenerado.pdf")
            .build();

        } else {
            //System.out.println("No se han encontrado ninguna violación en las cláusulas.");

            String messageOk = "No se han encontrado ninguna violación en las cláusulas.";
            return Response.ok(messageOk)
            .type("text/plain")  // Establecer explícitamente el tipo de contenido
            .build();
        }


        /*return Response.ok(outputFile)
                .header("Content-Disposition", "attachment; filename=ContratoGenerado.pdf")
                .build();*/
    }
}
//Hay que revisar como sacar la info de cada uno .