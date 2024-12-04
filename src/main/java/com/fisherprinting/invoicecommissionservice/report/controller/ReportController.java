package com.fisherprinting.invoicecommissionservice.report.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import com.fisherprinting.invoicecommissionservice.report.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/report/v1")
public class ReportController {

    private final CustomerLevelService customerLevelService;
    private final InvoiceLevelService invoiceLevelService;
    private final ReportService reportService;

    public ReportController(CustomerLevelService customerLevelService, InvoiceLevelService invoiceLevelService, ReportService reportService) {
        this.customerLevelService = customerLevelService;
        this.invoiceLevelService = invoiceLevelService;
        this.reportService = reportService;
    }


    @GetMapping("/salesEmployeeCommission")
    public DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo
    calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
                                   @RequestParam("invoiceID")int invoiceID,
                                   @RequestParam("taskID")int taskID,
                                   @RequestParam("orderNumber")int orderNumber,
                                   @RequestParam("employeeID")int employeeID) {
        CustomerLevelService.CustomerLevelCalculatedCommissionInfo customerLevelCommInfo = this.customerLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
        InvoiceLevelService.InvoiceLevelCalculatedCommissionInfo invoiceLevelCommInfo = this.invoiceLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);

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
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
    }

    // localhost:1118/invoiceCommissionService/report/v1/pdfdownload/123
    @GetMapping("/pdfdownload/{invoiceID}")
    public ResponseEntity<Resource> downloadContractPDF(@PathVariable("invoiceID") Integer invoiceID){
//        InputStreamResource resource = new InputStreamResource(reportService.generateInvoiceCommissionReport());
        InputStreamResource resource = new InputStreamResource(reportService.test1());
        String fileName = "testFileName.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
