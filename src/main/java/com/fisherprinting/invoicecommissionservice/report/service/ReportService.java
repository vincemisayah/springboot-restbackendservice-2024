package com.fisherprinting.invoicecommissionservice.report.service;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.CustomerInfo;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.report.dao.ReportDao;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import com.fisherprinting.invoicecommissionservice.subcontract.dao.SubcontractDao;
import com.fisherprinting.invoicecommissionservice.subcontract.service.SubcontractService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPCell;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import static com.itextpdf.kernel.pdf.PdfName.Border;


@Service
public class ReportService {
    private final ReportDao reportDao;
    private final InvoiceLevelDao invoiceLevelDao;
    private final CustomerLevelService customerLevelService;
    private final InvoiceLevelService invoiceLevelService;
    private final SubcontractDao subcontractDao;
    private final SubcontractService subcontractService;
    private final CustomerLevelDao customerLevelDao;

    public ReportService(ReportDao reportDao, InvoiceLevelDao invoiceLevelDao, CustomerLevelService customerLevelService, InvoiceLevelService invoiceLevelService, SubcontractDao subcontractDao, SubcontractService subcontractService, CustomerLevelDao customerLevelDao) {
        this.reportDao = reportDao;
        this.invoiceLevelDao = invoiceLevelDao;
        this.customerLevelService = customerLevelService;
        this.invoiceLevelService = invoiceLevelService;
        this.subcontractDao = subcontractDao;
        this.subcontractService = subcontractService;
        this.customerLevelDao = customerLevelDao;
    }


    public InputStream getSalespersonCommissionReport(int empID, LocalDate d1, LocalDate d2){

        String empName = reportDao.getEmployeeName(empID);

        // TODO: Filter out which invoices are paid
        List<Integer> invoiceIds = new ArrayList<>();
        invoiceIds.add(200001); // Change empID for this invoice to 633
        invoiceIds.add(208072);
        invoiceIds.add(209072);
        invoiceIds.add(209073);

        InputStream inputStream = null;
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Create Document and PdfWriter
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Paragraph p = new Paragraph();
            p.add("Salesperson: " + empName);
            p.add("\nInvoice Date Range: " + d1.toString() + " - " + d2.toString());
            p.add("\n");
            document.add(p);

            for(int i = 0; i < invoiceIds.size(); i++){
                // Check if the empID salesperson is assigned to the current invoice.
                CustomerInfo customerInfo = customerLevelService.getCustomerInfoByInvoiceId(invoiceIds.get(i));

                boolean salespersonIsAssignedToThisInvoice = false;
                for(SalesPerson s:customerInfo.getSalesPersonList()) {
                    if(s.salesPersonId == empID){
                        salespersonIsAssignedToThisInvoice = true;
                    }
                }
                if(!salespersonIsAssignedToThisInvoice){
                    p = new Paragraph();
                    p.add("\n");
                    Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
                    Chunk chunkInvoiceId = new Chunk((i+1) + ". INVOICE ID: " + invoiceIds.get(i) + ": NOT ASSIGNED", font);
                    p.add(chunkInvoiceId);
                    document.add(p);
                    continue;
                }

                int invoiceId = invoiceIds.get(i);
                DataTransferObjectsContainer.InvoiceInfo invoiceInfo = reportDao.getInvoiceInfo(invoiceId);
                BigDecimal invoiceTotal = reportDao.getInvoiceTotal(invoiceId);

                p = new Paragraph();
                p.add("\n");
                Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
                Chunk chunkInvoiceId = new Chunk((i+1) + ". INVOICE ID: " + invoiceId + "\n", font);
                p.add(chunkInvoiceId);

                Font fontTablElem = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
                p.setTabSettings(new TabSettings(20f));
                p.add(Chunk.TABBING);
                p.add(new Chunk("\u2022  Customer AR#: " + invoiceInfo.customerAR( ) + "\n", fontTablElem));
                p.setTabSettings(new TabSettings(20f));
                p.add(Chunk.TABBING);
                p.add(new Chunk("\u2022  Customer Name: " + invoiceInfo.customerName( ) + "\n", fontTablElem));
                p.setTabSettings(new TabSettings(20f));
                p.add(Chunk.TABBING);
                p.add(new Chunk("\u2022  Inv. Date: " + invoiceInfo.invoiceDate( ) + "\n", fontTablElem));
                p.setTabSettings(new TabSettings(20f));
                p.add(Chunk.TABBING);
                p.add(new Chunk("\u2022  Inv. Payment Due Date: " + invoiceInfo.paymentDueDate( ) + "\n\n", fontTablElem));
                document.add(p);

                List<InvoiceLevelDao.InvoiceChargedTaskItem> chargedTaskItems = invoiceLevelDao.getInvoiceChargedItems(invoiceId);

                BigDecimal salesCommissionTotal = new BigDecimal(0);


                int count = 0;
                for(InvoiceLevelDao.InvoiceChargedTaskItem item : chargedTaskItems){
                    DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo calculatedCommissionInfo = this.calculateInvoiceTaskCommission(
                            invoiceInfo.customerId(), invoiceId, item.taskId(), item.order(), empID);

                    salesCommissionTotal = (calculatedCommissionInfo != null)? salesCommissionTotal.add(calculatedCommissionInfo.salesDollarValue()):salesCommissionTotal.add(new BigDecimal(0));

                    PdfPTable table = new PdfPTable(9);
                    table.setTotalWidth(500);
                    table.setLockedWidth(true);

                    if(count < 1){
                        Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, BaseColor.BLACK);
                        PdfPCell c1 = new PdfPCell(new Paragraph("Task", tableHeaderFont));
                        PdfPCell c2 = new PdfPCell(new Paragraph("Dept", tableHeaderFont));
                        PdfPCell c3 = new PdfPCell(new Paragraph("Qty", tableHeaderFont));
                        PdfPCell c4 = new PdfPCell(new Paragraph("Cost", tableHeaderFont));
                        PdfPCell c5 = new PdfPCell(new Paragraph("Amount", tableHeaderFont));
                        PdfPCell c6 = new PdfPCell(new Paragraph("Task Rate", tableHeaderFont));
                        PdfPCell c7 = new PdfPCell(new Paragraph("Task Comm. Amt.", tableHeaderFont));
                        PdfPCell c8 = new PdfPCell(new Paragraph("Sales Assigned Rate", tableHeaderFont));
                        PdfPCell c9 = new PdfPCell(new Paragraph("Sales Comm. Amt.", tableHeaderFont));



                        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c4.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c5.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c5.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c6.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c6.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c7.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c7.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c8.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c8.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        c9.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c9.setVerticalAlignment(Element.ALIGN_MIDDLE);


                        table.addCell(c1);
                        table.addCell(c2);
                        table.addCell(c3);
                        table.addCell(c4);
                        table.addCell(c5);
                        table.addCell(c6);
                        table.addCell(c7);
                        table.addCell(c8);
                        table.addCell(c9);
                    }

                    String configLevel = "";
                    if(calculatedCommissionInfo != null && calculatedCommissionInfo.configLevel().equals("SUBCONTRACT LEVEL")){
                        configLevel = " (SC)";
                    }else if(calculatedCommissionInfo != null && calculatedCommissionInfo.configLevel().equals("INVOICE LEVEL")){
                        configLevel = " (INV)";
                    }else if(calculatedCommissionInfo != null && calculatedCommissionInfo.configLevel().equals("CUSTOMER LEVEL")){
                        configLevel = " (CST)";
                    }

                    Font tableFont = FontFactory.getFont(FontFactory.HELVETICA, 7, BaseColor.BLACK);
                    PdfPCell cell1 = new PdfPCell(new Paragraph(item.taskName() + configLevel, tableFont));
                    PdfPCell cell2 = new PdfPCell(new Paragraph(item.deptName(), tableFont));
                    PdfPCell cell3 = new PdfPCell(new Paragraph(new DecimalFormat("0.#").format(item.qty()), tableFont));
                    PdfPCell cell4 = new PdfPCell(new Paragraph("$" + String.valueOf(item.cost()), tableFont));
                    PdfPCell cell5 = new PdfPCell(new Paragraph("$" + String.valueOf(item.amount()), tableFont));
                    PdfPCell cell6 = new PdfPCell(new Paragraph("n/a", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK)));
                    PdfPCell cell7 = new PdfPCell(new Paragraph("n/a", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK)));
                    PdfPCell cell8 = new PdfPCell(new Paragraph("n/a", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK)));
                    PdfPCell cell9 = new PdfPCell(new Paragraph("n/a", FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK)));

                    cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell5.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    cell9.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell9.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    BaseColor LightGray = new BaseColor(215, 215, 215);
                    if(count%2 == 0){
                        cell1.setBackgroundColor(LightGray);
                        cell2.setBackgroundColor(LightGray);
                        cell3.setBackgroundColor(LightGray);
                        cell4.setBackgroundColor(LightGray);
                        cell5.setBackgroundColor(LightGray);
                        cell6.setBackgroundColor(LightGray);
                        cell7.setBackgroundColor(LightGray);
                        cell8.setBackgroundColor(LightGray);
                        cell9.setBackgroundColor(LightGray);
                    }


                    if(calculatedCommissionInfo != null){
                        cell6 = new PdfPCell(new Paragraph(String.valueOf(calculatedCommissionInfo.taskRate()) + "%", tableFont));
                        cell7 = new PdfPCell(new Paragraph("$" + String.valueOf(calculatedCommissionInfo.taskCommissionDollarValue()), tableFont));
                        cell8 = new PdfPCell(new Paragraph(String.valueOf(calculatedCommissionInfo.salesPersonAssignedRate()) + "%", tableFont));
                        cell9 = new PdfPCell(new Paragraph("$" + String.valueOf(calculatedCommissionInfo.salesDollarValue()), tableFont));

                        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);

                        cell7.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);

                        cell8.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);

                        cell9.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell9.setVerticalAlignment(Element.ALIGN_MIDDLE);

                        cell6.setPaddingRight(3f);
                        cell7.setPaddingRight(3f);
                        cell8.setPaddingRight(3f);
                        cell9.setPaddingRight(3f);

                        if(count%2 == 0){
                            cell6.setBackgroundColor(LightGray);
                            cell7.setBackgroundColor(LightGray);
                            cell8.setBackgroundColor(LightGray);
                            cell9.setBackgroundColor(LightGray);
                        }
                    }

                    table.addCell(cell1);
                    table.addCell(cell2);
                    table.addCell(cell3);
                    table.addCell(cell4);
                    table.addCell(cell5);
                    table.addCell(cell6);
                    table.addCell(cell7);
                    table.addCell(cell8);
                    table.addCell(cell9);


                    // Display total at the last row, final column
                    if(count == chargedTaskItems.size()-1){
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));
                        table.addCell(new PdfPCell(new Paragraph("")));

                        Font salesCommissionTotalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(11, 84, 30));
                        PdfPCell salesTotalCommission = new PdfPCell(new Paragraph("$"+ String.valueOf(salesCommissionTotal), salesCommissionTotalFont));
                        salesTotalCommission.setBackgroundColor(new BaseColor(193, 247, 207));
                        salesTotalCommission.setHorizontalAlignment(Element.ALIGN_CENTER);
                        salesTotalCommission.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(salesTotalCommission);
                    }

                    document.add(table);
                    ++count;
                }


            }




            document.close();
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        }catch (Exception e){
            e.printStackTrace();
        }
        return inputStream;
    }

    public InputStream test1(){
        InputStream inputStream = null;
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Create Document and PdfWriter
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);
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

    public DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo
    calculateInvoiceTaskCommission(int customerID,
                                   int invoiceID,
                                   int taskID,
                                   int orderNumber,
                                   int employeeID)
    {
        SubcontractService.SubcontractLevelCalculatedCommissionInfo subcontractLevelCommInfo = this.subcontractService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
        CustomerLevelService.CustomerLevelCalculatedCommissionInfo customerLevelCommInfo = this.customerLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
        InvoiceLevelService.InvoiceLevelCalculatedCommissionInfo invoiceLevelCommInfo = this.invoiceLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);


        // TODO: 1. Calculate the Task Rate Percentage (which in this case is the Subcontract Percentage).
        //  A. Acquire the following variables from Subcontract DAO
        //      VAR-A: <PO Price> of the linked invoiceID/TaskID
        //      VAR-B: <Summation of the 'amounts' of the invoice task items that have the same Task IDs>
        //
        //      Formula:
        //          Subcontract % = VAR-A / VAR-B
        if(subcontractLevelCommInfo != null){
            String configLevel = "SUBCONTRACT LEVEL";
            BigDecimal amount = subcontractLevelCommInfo.amount();
            BigDecimal taskRate = subcontractLevelCommInfo.taskRate();
            BigDecimal taskCommissionDollarValue = subcontractLevelCommInfo.taskCommissionDollarValue();
            BigDecimal salesPersonAssignedRate = subcontractLevelCommInfo.salesPersonAssignedRate();
            BigDecimal salesDollarValue = subcontractLevelCommInfo.salesDollarValue();
            String taskRateNote = subcontractLevelCommInfo.taskRateNote();
            String salesPersonAssignedRateNote = subcontractLevelCommInfo.salesPersonAssignedRateNote();
            String assignedBy = subcontractLevelCommInfo.assignedBy();

            return new DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo(
                    configLevel,amount,taskRate,
                    taskCommissionDollarValue,
                    salesPersonAssignedRate,salesDollarValue,
                    taskRateNote,salesPersonAssignedRateNote,assignedBy);
        }

        // At this point, since the Invoice ID is not linked to any PO,
        // we'll resort to using the Customer or Invoice Level Configurations to
        // calculate the salesperson's commission
        if(invoiceLevelCommInfo != null) {
            String configLevel = "INVOICE LEVEL";
            BigDecimal amount = invoiceLevelCommInfo.amount();
            BigDecimal taskRate = invoiceLevelCommInfo.taskRate();
            BigDecimal taskCommissionDollarValue = invoiceLevelCommInfo.taskCommissionDollarValue();
            BigDecimal salesPersonAssignedRate = invoiceLevelCommInfo.salesPersonAssignedRate();
            BigDecimal salesDollarValue = invoiceLevelCommInfo.salesDollarValue();
            String taskRateNote = invoiceLevelCommInfo.taskRateNote();
            String salesPersonAssignedRateNote = invoiceLevelCommInfo.salesPersonAssignedRateNote();
            String assignedBy = invoiceLevelCommInfo.assignedBy();

            return new DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo(
                    configLevel,amount,taskRate,
                    taskCommissionDollarValue,
                    salesPersonAssignedRate,salesDollarValue,
                    taskRateNote,salesPersonAssignedRateNote,assignedBy);
        }else if(customerLevelCommInfo != null) {
            String configLevel = "CUSTOMER LEVEL";
            BigDecimal amount = customerLevelCommInfo.amount();
            BigDecimal taskRate = customerLevelCommInfo.taskRate();
            BigDecimal taskCommissionDollarValue = customerLevelCommInfo.taskCommissionDollarValue();
            BigDecimal salesPersonAssignedRate = customerLevelCommInfo.salesPersonAssignedRate();
            BigDecimal salesDollarValue = customerLevelCommInfo.salesDollarValue();
            String taskRateNote = customerLevelCommInfo.taskRateNote();
            String salesPersonAssignedRateNote = customerLevelCommInfo.salesPersonAssignedRateNote();
            String assignedBy = customerLevelCommInfo.assignedBy();

            return new DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo(
                    configLevel,amount,taskRate,
                    taskCommissionDollarValue,
                    salesPersonAssignedRate,salesDollarValue,
                    taskRateNote,salesPersonAssignedRateNote,assignedBy);
        }else{
            // 404 code status is returned if customer-level config does not exist.
            // Prompt the user to create a customer-level config if this happens.
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND, "entity not found"
//            );
        }

        return null;
    }

    public List<SalesPerson> getSalespersonListByInvoiceID(int invoiceID){
        CustomerLevelDao.CustomerAndJobInfo customerInfo = customerLevelDao.getCustomerAndJobInfo(invoiceID);
        List<SalesPerson> salesPersonList = customerLevelDao.getSalesPersonListById(customerInfo.customerID()).getSalesPersonList();
        return salesPersonList;
    }

    public List<Integer> getSalespersonListFromInvoiceList(List<DTOs.PaidInvoiceInfo> paidInvoices){
        TreeSet<Integer> salesPersonTreeSet = new TreeSet<>();
        for(DTOs.PaidInvoiceInfo paidInvoice : paidInvoices){
            List<SalesPerson> salesPersonList = getSalespersonListByInvoiceID(paidInvoice.invoiceID());
            for(SalesPerson salesPerson : salesPersonList){
                salesPersonTreeSet.add(salesPerson.salesPersonId);
            }
        }
        return new ArrayList<>(salesPersonTreeSet);
    }
}
