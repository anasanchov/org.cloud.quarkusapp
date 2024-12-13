package org.acme;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.nio.file.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.glassfish.jersey.media.multipart.FormDataParam;
import jakarta.ws.rs.Path;


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

        // Cargar la plantilla y rellenarla con los datos
        PdfGenerator pdfGenerator = new PdfGenerator();
        String templateText = pdfGenerator.loadTemplate();
        String content = pdfGenerator.fillTemplate(templateText, pdfFormFiller.sellerDict);
        content = pdfGenerator.fillTemplate(content, pdfFormFiller.buyerDict);

        /*String templateText = pdf_generator.loadTemplate();
        String content = pdf_generator.fillTemplate(templateText,pdf_form_filler.sellerDict);
        content = pdf_generator.fillTemplate(content,pdf_form_filler.buyerDict);*/

        // Crear el PDF de salida
        String outputFilePath = "src/tmp/tempContrato.pdf";
        pdfGenerator.createPdf(content);

        // Devolver el archivo generado como respuesta
        File outputFile = new File(outputFilePath);
        return Response.ok(outputFile)
                .header("Content-Disposition", "attachment; filename=ContratoGenerado.pdf")
                .build();
    }
}
//Hay que revisar como sacar la info de cada uno .