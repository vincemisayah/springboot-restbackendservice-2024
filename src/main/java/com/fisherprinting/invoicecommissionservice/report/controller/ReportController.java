package com.fisherprinting.invoicecommissionservice.report.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.report.dao.ReportDao;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import com.fisherprinting.invoicecommissionservice.report.service.ReportService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/report/v1")
public class ReportController {

    private final CustomerLevelService customerLevelService;
    private final InvoiceLevelService invoiceLevelService;
    private final ReportService reportService;
    private final FileUploadService fileUploadService;
    private final ReportDao reportDao;

    public ReportController(CustomerLevelService customerLevelService, InvoiceLevelService invoiceLevelService, ReportService reportService, FileUploadService fileUploadService, ReportDao reportDao) {
        this.customerLevelService = customerLevelService;
        this.invoiceLevelService = invoiceLevelService;
        this.reportService = reportService;
        this.fileUploadService = fileUploadService;
        this.reportDao = reportDao;
    }


    @GetMapping("/salesEmployeeCommission")
    public DataTransferObjectsContainer.FinalSalesCalculatedCommissionInfo
    calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
                                   @RequestParam("invoiceID")int invoiceID,
                                   @RequestParam("taskID")int taskID,
                                   @RequestParam("orderNumber")int orderNumber,
                                   @RequestParam("employeeID")int employeeID) {
        return reportService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
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

    @PostMapping("/paidInvoices")
    public ResponseEntity<?> paidInvoices(@RequestParam("startDate") @DateTimeFormat(pattern = "M/d/yy") LocalDate startDate,
                                          @RequestParam("endDate")   @DateTimeFormat(pattern = "M/d/yy") LocalDate endDate) throws ParseException {
        List<DTOs.PaidInvoiceInfo> paidInvoices = fileUploadService.removeDuplicates(reportDao.getPaidInvoicesFromRecords(startDate, endDate));
        List<Integer> salesPersonEmpIDs = reportService.getSalespersonListFromInvoiceList(paidInvoices);
        List<DTOs.SalespersonAssignedInvoices> salespersonAssignedInvoices = new ArrayList<>();

        for(Integer empID : salesPersonEmpIDs){
            String empName = reportDao.getEmployeeNameByID(empID);
            DTOs.Salesperson salesperson = new DTOs.Salesperson(empID, empName);
            List<DTOs.PaidInvoiceInfo> paidInvoicesAssignedToEmpId = reportDao.getPaidInvoicesFromRecordsByEmpID(empID, startDate, endDate);
            salespersonAssignedInvoices.add(new DTOs.SalespersonAssignedInvoices(salesperson, fileUploadService.viewableFilteredInvoiceData(paidInvoicesAssignedToEmpId)));
        }
        List<DTOs.ViewableFilteredInvoiceData> viewablePaidInvoices = fileUploadService.viewableFilteredInvoiceData(paidInvoices);

        return ResponseEntity.ok().body(Map.of(
                "PaidInvoices", viewablePaidInvoices,
                "SalespersonAssignedInvoices", salespersonAssignedInvoices));
    }

    @PostMapping("/viewSalespersonPdfReport/{empID}")
    public ResponseEntity<?> viewSalespersonPdfReport(@PathVariable("empID") Integer empID, @RequestParam("invoiceList") List<Integer> invoiceIDList) throws ParseException {
        InputStreamResource resource = new InputStreamResource(reportService.getSalespersonCommissionReport(empID, invoiceIDList));

        String empName = reportDao.getEmployeeNameByID(empID).replaceAll(" ", "");
        String fileName = empName + "-CommissionReport.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("/saveToBatchReport")
    public ResponseEntity<?> saveBatchReport(@RequestParam("startDate") @DateTimeFormat(pattern = "M/d/yy") LocalDate startDate,
                                          @RequestParam("endDate")   @DateTimeFormat(pattern = "M/d/yy") LocalDate endDate) throws ParseException, DocumentException, IOException {
        List<DTOs.PaidInvoiceInfo> paidInvoices = fileUploadService.removeDuplicates(reportDao.getPaidInvoicesFromRecords(startDate, endDate));
        List<Integer> salesPersonEmpIDs = reportService.getSalespersonListFromInvoiceList(paidInvoices);
        List<DTOs.SalespersonAssignedInvoices> salespersonAssignedInvoices = new ArrayList<>();

        List<Integer> empIds = new ArrayList<>();
        for(Integer empID : salesPersonEmpIDs){
            empIds.add(empID);

            String empName = reportDao.getEmployeeNameByID(empID);
            DTOs.Salesperson salesperson = new DTOs.Salesperson(empID, empName);
            List<DTOs.PaidInvoiceInfo> paidInvoicesAssignedToEmpId = reportDao.getPaidInvoicesFromRecordsByEmpID(empID, startDate, endDate);
            salespersonAssignedInvoices.add(new DTOs.SalespersonAssignedInvoices(salesperson, fileUploadService.viewableFilteredInvoiceData(paidInvoicesAssignedToEmpId)));
        }
        List<DTOs.ViewableFilteredInvoiceData> viewablePaidInvoices = fileUploadService.viewableFilteredInvoiceData(paidInvoices);


        InputStreamResource resource = new InputStreamResource(reportService.createBatchReportt(salespersonAssignedInvoices));

        Document document = new Document();

        return ResponseEntity.ok().body(Map.of(
                "PaidInvoices", viewablePaidInvoices,
                "SalespersonAssignedInvoices", salespersonAssignedInvoices));
    }
}
