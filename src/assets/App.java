package mma;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
        // boolean isFinished = pdf_form_filler.loadPdfData("/Users/marmonteranduix/Documents/MASTER/ASSIGNATURES/CLOUD COMPUTING/PROJECTE/COMPRA-VENDA_PLENA.pdf");
        boolean isFinished = pdf_form_filler.loadPdfData("src/assets/COMPRA-VENDA_PLENA.pdf");

        if(isFinished){
            
            String templateText = pdf_generator.loadTemplate();
            String content = pdf_generator.fillTemplate(templateText,pdf_form_filler.sellerDict);
            content = pdf_generator.fillTemplate(content,pdf_form_filler.buyerDict);

            pdf_generator.createPdf(content, "src/demandas/TESTEO.pdf");
        }
    }
}
