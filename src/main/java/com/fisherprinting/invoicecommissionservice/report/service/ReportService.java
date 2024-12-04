package com.fisherprinting.invoicecommissionservice.report.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

@Service
public class ReportService {

    public InputStream test1(){
        InputStream inputStream = null;
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Create Document and PdfWriter
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
            Chunk chunk = new Chunk("Hello World", font);

            document.add(chunk);
            document.close();
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception e){
            e.printStackTrace();
        }
        return inputStream;
    }

    public <T>ByteArrayInputStream generateInvoiceCommissionReport(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{

            // Create a document and add a page to it
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage( page );

            // Create a new font object selecting one of the PDF base fonts

            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            int xStartOffset = 40;
            int yStartOffset = 750;
            int nextLineOffset = 12;

            LinkedList<String> pdfStringConent = new LinkedList<String>();
            pdfStringConent.push("Salesperson: Bill Woodlock");
            pdfStringConent.push("Invoice Date: 9/17/2024 - 12/05/2024");

            for(String content : pdfStringConent){
                contentStream.beginText();
                contentStream.setFont( new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10 );
                contentStream.newLineAtOffset( xStartOffset, yStartOffset );
                contentStream.showText(content);
                contentStream.endText();

                yStartOffset -= nextLineOffset;
            }

            LinkedList<String> invoiceInfo = new LinkedList<String>();
            invoiceInfo.push("Invoice ID: 2008072");
            invoiceInfo.push("Customer AR #: 91824");
            invoiceInfo.push("Customer Name: A TEST CUSTOMER");
            invoiceInfo.push("Inv Date: 09/18/2024");
            invoiceInfo.push("Payment Due Date: 10/09/2024");
            invoiceInfo.push("Inv. Total: $695.00");
            invoiceInfo.push("Inv. Amount Paid: $695.00");
            yStartOffset -= nextLineOffset;
            for(String content : invoiceInfo){
                contentStream.beginText();
                contentStream.setFont( new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10 );
                contentStream.newLineAtOffset( xStartOffset, yStartOffset );
                contentStream.showText(content);
                contentStream.endText();

                yStartOffset -= nextLineOffset;
            }



            // Define a text content stream using the selected font, and print the text
//            contentStream.beginText();
//            contentStream.setFont( new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10 );
//            contentStream.newLineAtOffset( 40, 750 );
//            contentStream.showText( "Salesperson: Bill Woodlock");
//            contentStream.endText();
//
//            contentStream.beginText();
//            contentStream.setFont( new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10 );
//            contentStream.newLineAtOffset( 40, 738 );
//            contentStream.showText( "Invoice Date: 9/17/2024 - 12/05/2024");
//            contentStream.endText();

            //  closed the content stream class.
            contentStream.close();

            // Save the results and ensure that the document is properly closed.
            document.save( outputStream );
            document.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
