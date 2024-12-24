package com.fisherprinting.invoicecommissionservice.report.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.report.dao.ReportDao;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import com.fisherprinting.invoicecommissionservice.report.service.ReportService;
import com.itextpdf.text.DocumentException;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import java.io.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/report/v1")
public class ReportController {

    private final CustomerLevelService customerLevelService;
    private final InvoiceLevelService invoiceLevelService;
    private final ReportService reportService;
    private final FileUploadService fileUploadService;
    private final ReportDao reportDao;

    @Value("${app.records.path}")
    private String basePath;

    @Value("${app.records.defaultBatchReportFileName}")
    private String fileName;

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


        InputStreamResource resource = new InputStreamResource(reportService.createAndSaveBatchReport(salespersonAssignedInvoices));
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = ft.format(new Date());
        String filePath = basePath + dateStr + "/" + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            return ResponseEntity.ok().body(Map.of("Message", "Success"));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("Message", "Failed"));
        }
    }

    @GetMapping("/savedBatchReports/{year}")
    public ResponseEntity<?> getSavedBatchReports(@PathVariable("year") int year) {
        Map<String, List<DataTransferObjectsContainer.SavedBatchReport>> filesByMonth = Map.ofEntries(
                Map.entry("January", reportService.fileNamesByMonth(year, 1)),
                Map.entry("February", reportService.fileNamesByMonth(year, 2)),
                Map.entry("March", reportService.fileNamesByMonth(year, 3)),
                Map.entry("April", reportService.fileNamesByMonth(year, 4)),
                Map.entry("May", reportService.fileNamesByMonth(year, 5)),
                Map.entry("June", reportService.fileNamesByMonth(year, 6)),
                Map.entry("July", reportService.fileNamesByMonth(year, 7)),
                Map.entry("August", reportService.fileNamesByMonth(year, 8)),
                Map.entry("September", reportService.fileNamesByMonth(year, 9)),
                Map.entry("October", reportService.fileNamesByMonth(year, 10)),
                Map.entry("November", reportService.fileNamesByMonth(year, 11)),
                Map.entry("December", reportService.fileNamesByMonth(year, 12))
        );

        return ResponseEntity.ok().body(filesByMonth);
    }

    public static InputStreamResource convertFileToInputStreamResource(File file) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);
        return new InputStreamResource(inputStream);
    }

    @PostMapping("/download/batchReport")
    public ResponseEntity<?> viewSalespersonPdfReport(@RequestParam("passedDate") String passedDate){
        String filePath = basePath + passedDate + "/" + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            try {
                InputStreamResource resource = convertFileToInputStreamResource(file);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=" + fileName)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + e.getMessage());
            }
        } else {
            // Handle error if file not found
        }
        return ResponseEntity.internalServerError().body(Map.of("message", "File not found"));
    }
}
