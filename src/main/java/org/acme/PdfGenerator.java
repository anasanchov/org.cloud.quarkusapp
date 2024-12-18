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
import java.util.ArrayList;
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

    public static String fillTemplate(String template, Dictionary<String, String> data) {
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

    public static String fillArticles(String template, List<Map<String, String>> articlesWithLaws) {
        // Añadir los artículos y leyes en el apartado VII
        StringBuilder fondoDeLaPretension = new StringBuilder();
    
        // Máxima longitud de línea para el ajuste de texto
        int maxLineLength = 90; // Ajustar según lo necesario para el tamaño del espacio en tu PDF
    
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
        template = template.replace("{{articulo}}{{texto_articulo}}", fondoDeLaPretension.toString());
    
        return template;
    }
    
    // Método para dividir el texto largo en líneas de longitud adecuada
    private static String wrapText(String text, int maxLineLength) {
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
    
        // Definir márgenes
        float marginTop = 72;     // 2.5 cm = 72 puntos
        float marginBottom = 72;  // 2.5 cm
        float marginLeft = 72; 
    
        // Configuración de página A4
        PDRectangle pageSize = PDRectangle.A4;
        //float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();
    
        // Configuración de fuente
        PDType1Font font = PDType1Font.TIMES_ROMAN;
        float fontSize = 12;
        float lineHeight = fontSize + 2; // Altura de línea
    
        // Preparar contenido
        String[] lines = content.split("\n");
    
        // Lista para almacenar líneas procesadas
        List<String> processedLines = new ArrayList<>();
        for (String line : lines) {
            String cleanLine = removeSpecialCharacters(line);
            if (!cleanLine.isEmpty()) {
                processedLines.add(cleanLine);
            }
        }
    
        // Variables para gestión de páginas
        int lineIndex = 0;
        boolean isFirstPage = true;  // Para identificar la primera página
    
        // Iniciar ciclo para procesar las páginas
        while (lineIndex < processedLines.size()) {
            // Crear nueva página
            PDPage currentPage = new PDPage(pageSize);
            document.addPage(currentPage);
    
            // Iniciar stream de contenido
            try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage)) {
                contentStream.beginText();
    
                float currentY = pageHeight - marginTop;
                contentStream.newLineAtOffset(marginLeft, currentY); // Coloca el cursor al principio
    
                // Imprimir la primera línea de la página
                String lineToPrint = processedLines.get(lineIndex);
    
                if (isFirstPage) {
                    // La primera página, primera línea en negrita y tamaño 14
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    isFirstPage = false;  // Después de la primera página, cambiamos el estado
                } else {
                    // En las siguientes páginas, primera línea normal y tamaño 12
                    contentStream.setFont(font, fontSize);
                }
    
                // Imprimir la línea
                contentStream.showText(lineToPrint);
                lineIndex++;  // Aumentar el índice para la siguiente línea
                currentY -= lineHeight + 10;  // Ajustar espacio después de la primera línea (mayor espacio)
                contentStream.newLineAtOffset(0, -(lineHeight + 10)); // Mover al siguiente texto
    
                // Volver a la fuente normal para el resto del contenido
                contentStream.setFont(font, fontSize);
    
                // Imprimir las siguientes líneas en la página
                while (lineIndex < processedLines.size() && currentY > marginBottom) {
                    String nextLine = processedLines.get(lineIndex);
    
                    // Verificar si la línea está completamente en mayúsculas
                    if (nextLine.equals(nextLine.toUpperCase())) {
                        // Añadir salto de línea antes de la línea en mayúsculas
                        currentY -= lineHeight;  // Ajusta el salto de línea
                        contentStream.newLineAtOffset(0, -lineHeight); // Mueve al siguiente espacio
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12); // Negrita, tamaño 12
                    } else {
                        contentStream.setFont(font, fontSize); // Fuente normal, tamaño 12
                    }
    
                    contentStream.showText(nextLine);
    
                    // Mover a siguiente línea (actualizar currentY)
                    currentY -= lineHeight;
                    contentStream.newLineAtOffset(0, -lineHeight);
    
                    lineIndex++;
                }
    
                contentStream.endText(); // Finaliza el bloque de texto
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // Guardar documento
        document.save(outputFilePath);
        document.close();
    }
    
}
