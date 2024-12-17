package org.acme;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

public class PdfGenerator {
    /*
    public String loadTemplate() {
        // Aquí cargamos la plantilla de texto que servirá para llenar los datos
        return "Contrato de compra-venta\nVendedor: {{seller_name}}\nDirección: {{seller_street}} {{seller_num}}, {{seller_city}} {{seller_CP}}\nComprador: {{buyer_name}}\nDirección: {{buyer_street}} {{buyer_num}}, {{buyer_city}} {{buyer_CP}}";
    } */

    /*public String fillTemplate(String template, Map<String, String> data) {
        // Rellenar la plantilla con los datos proporcionados
        for (Map.Entry<String, String> entry : data.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }*/

    //public final static String PDF_PATH = "src/assets/DEMANDA JV - SIN POSTULACIÓN.pdf";
    public final static String PDF_PATH = "src/assets/plantilla.pdf";

    //transformamos el pdf en un String para poder modificarlo
    public static String loadTemplate() throws IOException{
        File file = new File(PDF_PATH);
        //PDDocument doc = Loader.loadPDF(file);
        PDDocument doc = PDDocument.load(file);

        PDFTextStripper stripper = new PDFTextStripper();
        return  stripper.getText(doc);
    }

    public String fillTemplate(String template, Dictionary<String, String> data) {
    // Rellenar la plantilla con los datos proporcionados
        Enumeration<String> keys = data.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = data.get(key);
            template = template.replace("{{" + key + "}}", value);
        }
        return template;
    }  

    public void createPdf(String content) throws IOException {
        // Especificar la ruta en el directorio /tmp
        String outputFilePath = "src/tmp/tempContrato.pdf";
        //System.out.println(content);
        
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
