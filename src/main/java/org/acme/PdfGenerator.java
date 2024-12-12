package org.acme;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PdfGenerator {

    public String loadTemplate() {
        // Aquí cargamos la plantilla de texto que servirá para llenar los datos
        return "Contrato de compra-venta\nVendedor: {{sellerName}}\nDirección: {{sellerAddress}}\nComprador: {{buyerName}}\nDirección: {{buyerAddress}}";
    }

    public String fillTemplate(String template, Map<String, String> data) {
        // Rellenar la plantilla con los datos proporcionados
        for (Map.Entry<String, String> entry : data.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    public void createPdf(String content) throws IOException {
        // Especificar la ruta en el directorio /tmp
        String outputFilePath = "src/tmp/tempContrato.pdf";
        
        // Asegurarse de que el directorio /tmp exista
        File outputFile = new File(outputFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // Crea el directorio si no existe
        }

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, 750);

        String[] lines = content.split("\n");
        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -15);  // Salto de línea
        }

        contentStream.endText();
        contentStream.close();

        // Guardar el documento PDF en /tmp
        document.save(outputFile);
        document.close();
    }
}
