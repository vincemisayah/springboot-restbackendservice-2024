package com.fisherprinting.invoicecommissionservice.report.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.report.dao.ReportDao;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import com.fisherprinting.invoicecommissionservice.report.service.ReportService;
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

    // localhost:1118/invoiceCommissionService/report/v1/pdfDownload/salespersonCommReport/291?d1=2024-01-05&d2=2024-12-05
    @GetMapping("/pdfDownload/salespersonCommReport/{empID}")
    public ResponseEntity<Resource> downloadSalespersonCommissionReport(
            @PathVariable("empID") Integer empID,
            @RequestParam("d1")  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate d1,
            @RequestParam("d2")  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate d2)
    {
        // Test Parameters
        int employeeId = empID;
        List<Integer> invoiceIds = new ArrayList<>();
        invoiceIds.add(2008072);

        InputStreamResource resource = new InputStreamResource(reportService.getSalespersonCommissionReport(employeeId, d1, d2));
        String fileName = "testFileName.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    // localhost:1118/invoiceCommissionService/report/v1/viewpdf/salespersonCommReport/291?d1=2024-01-05&d2=2024-12-05
    @GetMapping("/viewpdf/salespersonCommReport/{empID}")
    public ResponseEntity<Resource> viewSalespersonCommissionReport(
            @PathVariable("empID") Integer empID,
            @RequestParam("d1")  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate d1,
            @RequestParam("d2")  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate d2)
    {
        // Test Parameters
        int employeeId = empID;
        List<Integer> invoiceIds = new ArrayList<>();
        invoiceIds.add(2008072);

        InputStreamResource resource = new InputStreamResource(reportService.getSalespersonCommissionReport(employeeId, d1, d2));
        String fileName = "testFileName.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

//    @PostMapping("/paidInvoices")
//    public ResponseEntity<Resource> paidInvoices(@RequestParam("startDate") @DateTimeFormat(pattern = "M/d/yy") LocalDate startDate,
//                                          @RequestParam("endDate") @DateTimeFormat(pattern = "M/d/yy") LocalDate endDate){
////        return ResponseEntity.ok().body(Map.of("msg", "success"));
//
//        InputStreamResource resource = new InputStreamResource(reportService.getSalespersonCommissionReport(291, startDate, endDate));
//        String fileName = "testFileName.pdf";
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=" + fileName)
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(resource);
//    }

    @PostMapping("/paidInvoices")
    public ResponseEntity<?> paidInvoices(@RequestParam("startDate") @DateTimeFormat(pattern = "M/d/yy") LocalDate startDate,
                                          @RequestParam("endDate")   @DateTimeFormat(pattern = "M/d/yy") LocalDate endDate) throws ParseException {
        List<DTOs.PaidInvoiceInfo> paidInvoices = fileUploadService.removeDuplicates(reportDao.getPaidInvoicesFromRecords(startDate, endDate));

        List<Integer> salesPersonEmpIDs = reportService.getSalespersonListFromInvoiceList(paidInvoices);

        List<DTOs.ViewableFilteredInvoiceData> viewablePaidInvoices = fileUploadService.viewableFilteredInvoiceData(paidInvoices);

        Map<String, List<DTOs.PaidInvoiceInfo>> map = new HashMap<>();
        for(Integer empID : salesPersonEmpIDs){
            List<DTOs.PaidInvoiceInfo> paidInvoicesAssignedToEmpId = reportDao.getPaidInvoicesFromRecordsByEmpID(empID, startDate, endDate);
            map.put(empID.toString(), paidInvoicesAssignedToEmpId);
        }

        return ResponseEntity.ok().body(Map.of(
                "PaidInvoices", viewablePaidInvoices,
                "salesPersonEmpIDs", salesPersonEmpIDs,
                "assignedInvoicesPerSalesperson", map));
    }
}
