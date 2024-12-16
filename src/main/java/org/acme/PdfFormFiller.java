package org.acme;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
public class PdfFormFiller {
    public static final String INIT_SELLER_TEXT = "VENDEDORA:";
    public static final String END_SELLER_TEXT = "De otra parte";
    public static final String INIT_BUYER_TEXT = "COMPRADORA:";
    public static final String END_BUYER_TEXT = "Ambas partes";
    public static final String INIT_CLAUS_TEXT = "ESTIPULACIONES";
    public static final String END_CLAUS_TEXT = "Y para que así conste";


    public static Dictionary<String, String> sellerDict;
    public static Dictionary<String, String> buyerDict;


    public static boolean loadPdfData(String url) throws Exception {
        File file = new File(url);
        PDDocument doc = PDDocument.load(file);

        //PDDocument doc = Loader.loadPDF(file);

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);

        int initSeller = text.indexOf(INIT_SELLER_TEXT) + INIT_SELLER_TEXT.length();
        int endSeller = text.indexOf(END_SELLER_TEXT);

        int initBuyer = text.indexOf(INIT_BUYER_TEXT) + INIT_BUYER_TEXT.length();
        int endBuyer = text.indexOf(END_BUYER_TEXT);

        String sellerInfo = text.substring(initSeller, endSeller);
        String buyerInfo = text.substring(initBuyer, endBuyer);

        sellerDict = getInfoMap(sellerInfo,false);
        buyerDict = getInfoMap(buyerInfo, true);

        //System.out.println(INIT_SELLER_TEXT);
        //System.out.println(sellerDict.toString());
        //System.out.println(INIT_BUYER_TEXT);
        //System.out.println(buyerDict.toString());

        // testerino
        // String[] name = getNameAndSurnamesFromFullnam(sellerDict.get("name"));

        // for(int i = 0 ; i < name.length;i++){
        // System.out.println(name[i]);
        // }

        doc.close();

        return true;
    }

    private static Dictionary<String, String> getInfoMap(String info, boolean isBuyer) {
        Dictionary<String, String> dict = new Hashtable<>();

        String[] pepet = info.split(",");
        /// condicional ternari equivaldira a ->
        /// if(isBuyer){prefix = "buyer"} else { prefix = "seller"}
        String prefix = isBuyer ? "buyer" : "seller";


        dict.put(interpolateStrings(prefix,"name"), getFullName(pepet[0]));
        dict.put(interpolateStrings(prefix,"DNI"), getDni(pepet[1]));
        dict.put(interpolateStrings(prefix,"city"), getCity(pepet[2]));
        dict.put(interpolateStrings(prefix,"street"), getStreet(pepet[3]));
        dict.put(interpolateStrings(prefix,"num"), getNum(pepet[4]));
        dict.put(interpolateStrings(prefix,"CP"), getCP(pepet[5]));

        // recuperar valors del dict --> dict.get("name");

        return dict;
    }

    private static String interpolateStrings(String prefix, String variable ){
        return String.format("%s_%s", prefix, variable);
    }

    private static String getFullName(String sentence) {
        return sentence.replace("D. ", "").replace("Dª. ", "");
    }

    private static String[] getNameAndSurnamesFromFullnam(String fullName) {
        return fullName.split(" ");
    }

    private static String getDni(String sentece) {
        return sentece.replace("con N.I.F. nº ", "");
    }

    private static String getCity(String sentece) {
        return sentece.replace("y domicilio en ", "");
    }

    private static String getStreet(String sentence) {
        return sentence.replace("calle ", "");
    }

    private static String getNum(String sentence) {
        return sentence.replace("nº ", "");
    }

    private static String getCP(String sentence) {
        return sentence.replace("C.P. ", "").trim();
    }
    public static String[] getClausesArrayFromText(String text) {

        int initClause = text.indexOf(INIT_CLAUS_TEXT) + INIT_CLAUS_TEXT.length();
        int endClause = text.indexOf(END_CLAUS_TEXT);

        String clauseInfo = text.substring(initClause, endClause);

        String[] clauses = clauseInfo.split(".-");
        String[] result = new String[clauses.length - 1];

        // comencem per el 1 perque el primer item dek array es PRIMERA
        for (int i = 1; i < clauses.length; i++) {
            int index = clauses[i].lastIndexOf(".") + 1;
            result[i - 1] = clauses[i].substring(0, index).trim();
        }

        return result;
    }
    public static String extractTextFromPdf(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        if (!file.exists() || !file.getName().endsWith(".pdf")) {
            throw new IllegalArgumentException("El archivo no es un PDF válido o no existe.");
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

}