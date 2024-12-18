package org.acme;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.List;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

public class PdfGenerator {

    public final static String PDF_PATH = "src/assets/DEMANDA JV - SIN POSTULACIÓN.pdf";

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
            String value = data.get(key).replace("\n", "").trim(); // Elimina saltos de línea y espacios
            template = template.replace("{{" + key + "}}", value);
        }

        // Obtener la fecha actual y formatearla (por ejemplo, "dd/MM/yyyy")
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Reemplazar el marcador {{date}} con la fecha actual, si no se ha hecho ya
        template = template.replace("{{date}}", currentDate);
        return template;
    }

    public String fillArticles(String template, List<Map<String, String>> articlesWithLaws) {
        // Añadir los artículos y leyes en el apartado VII
        StringBuilder fondoDeLaPretension = new StringBuilder();
    
        // Máxima longitud de línea para el ajuste de texto
        int maxLineLength = 80; // Ajustar según lo necesario para el tamaño del espacio en tu PDF
    
        // Iterar sobre los artículos y leyes
        for (Map<String, String> articleWithLaw : articlesWithLaws) {
            String article = articleWithLaw.get("article");
            String ley = articleWithLaw.get("ley");
    
            // Ajustar el texto largo de la ley a líneas más pequeñas
            String wrappedLey = wrapText(ley, maxLineLength);
    
            // Añadir el artículo y la ley ajustada al resultado
            fondoDeLaPretension.append("- Artículo ").append(article).append(": ").append(wrappedLey).append("\n\n");
        }
    
        // Reemplazar el marcador {articulo}{texto_articulo} en el template
        template = template.replace("{articulo}{texto_articulo}", fondoDeLaPretension.toString());
    
        return template;
    }
    
    // Método para dividir el texto largo en líneas de longitud adecuada
    private String wrapText(String text, int maxLineLength) {
        StringBuilder wrappedText = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
    
        // Recorrer las palabras y formar líneas
        for (String word : words) {
            // Si añadir la palabra excede el tamaño máximo, iniciar una nueva línea
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                wrappedText.append(currentLine.toString()).append("\n");
                currentLine = new StringBuilder(word); // Iniciar nueva línea con la palabra actual
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" "); // Añadir espacio entre palabras
                }
                currentLine.append(word); // Añadir palabra a la línea
            }
        }
    
        // Añadir la última línea si no está vacía
        if (currentLine.length() > 0) {
            wrappedText.append(currentLine.toString());
        }
    
        return wrappedText.toString();
    }
    
    

    private static String removeSpecialCharacters(String test) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < test.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i))) {
                b.append(test.charAt(i));
            }
        }
        return b.toString();
    }

    public static void createPdf(String content, String outputFilePath) throws IOException {
        PDDocument document = new PDDocument();

        PDPage page = new PDPage(PDRectangle.A4);

        document.addPage(page);

        // Seleccionar la fuente Times Roman de PDType1Font
        PDType1Font font = PDType1Font.TIMES_ROMAN;
        
        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Define a text content stream using the selected font, moving the cursor and
        // drawing the text "Hello World"
        contentStream.beginText();
        contentStream.setFont(font, 12);
        String[] textList = content.split("\n");
        int y = 800;
        contentStream.newLineAtOffset(25, 800);
        for(int i =0; i < textList.length;i++ ){

            //contentStream.newLineAtOffset(100, y);
            contentStream.showText(removeSpecialCharacters(textList[i]));
            contentStream.newLineAtOffset(0, -15);
            
            y -= 15;

            if(y <= 0){
                
                PDPage page2 = new PDPage(PDRectangle.A4);
                document.addPage(page2);
                contentStream.endText();
                contentStream.close();
                contentStream = new PDPageContentStream(document, page2);
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(25, 800);
                y = 800;
                //contentStream.newLineAtOffset(25, 800);
            }
        }
        
        contentStream.endText();

        // Make sure that the content stream is closed:
        contentStream.close();

        document.save(outputFilePath);// File path);
        document.close();
    }
}
