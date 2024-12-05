package com.fisherprinting.invoicecommissionservice.invoiceLevel.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/invoiceLevel")
public class InvoiceLevelController {
    private final InvoiceLevelDao invoiceLevelDao;
    private final InvoiceLevelService invoiceLevelService;
    public InvoiceLevelController(InvoiceLevelDao invoiceLevelDao, InvoiceLevelService invoiceLevelService) {
        this.invoiceLevelDao = invoiceLevelDao;
        this.invoiceLevelService = invoiceLevelService;
    }

    @GetMapping("/invoiceSearchResult")
    public List<InvoiceLevelDao.InvoiceSearchResult> invoiceSearchResult(@RequestParam("invoiceID") int invoiceID) {
        return invoiceLevelDao.getInvoiceById(invoiceID);
    }

    @GetMapping("/invoiceChargedTaskItems")
    public List<InvoiceLevelDao.InvoiceChargedTaskItem> getInvoiceList(@RequestParam("invoiceId") int invoiceId) {
        return invoiceLevelDao.getInvoiceChargedItems(invoiceId);
    }

    // Currently at Customer Level only
    @GetMapping("/calculatedInvoiceTaskCommission")
    public InvoiceLevelService.InvoiceLevelCalculatedCommissionInfo
    calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
                                   @RequestParam("invoiceID")int invoiceID,
                                   @RequestParam("taskID")int taskID,
                                   @RequestParam("orderNumber")int orderNumber,
                                   @RequestParam("employeeID")int employeeID) {
        return invoiceLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
    }



    @GetMapping("/invoiceTaskRateInfo")
    public InvoiceLevelDao.TaskRateInfo TaskRateInfo(@RequestParam("invoiceID") int invoiceID, @RequestParam("taskID") int taskID) {
        return invoiceLevelDao.getTaskRateInfo(invoiceID, taskID);
    }

    @GetMapping("/employeeAssignedRate")
    public InvoiceLevelDao.EmployeeTaskRateInfo EmployeeRateInfo(@RequestParam("invoiceID") int invoiceID, @RequestParam("empID") int empID, @RequestParam("taskID") int taskID) {
        return invoiceLevelDao.getEmployeeTaskRateInfo(invoiceID, empID, taskID);
    }

    @GetMapping("/invoiceDistinctTaskItems")
    public List<InvoiceLevelDao.InvoiceTaskItem> EmployeeRateInfo(@RequestParam("invoiceID") int invoiceID) {
        return invoiceLevelDao.getDistinctChargedInvoiceTaskItems(invoiceID);
    }

    public record EmpInfo(int empID, BigDecimal salesRate, String note) { }

    public record InvoiceTaskConfig(
            int lastEditedBy,
            int customerID,
            int invoiceID,
            int taskID,
            BigDecimal taskRate,
            boolean active,
            String notes,
            List<EmpInfo> empRates) { }
    @PostMapping("/saveInvoiceTaskConfig")
    public ResponseEntity<String> saveInvoiceTaskConfig(@RequestBody InvoiceTaskConfig invoiceTaskConfig) throws InterruptedException {
        invoiceLevelDao.saveTaskConfiguration(invoiceTaskConfig);
        invoiceTaskConfig.empRates.forEach(emp->{
            invoiceLevelDao.saveEmployeeConfig(invoiceTaskConfig.invoiceID,
                    invoiceTaskConfig.taskID(),
                    emp.empID,
                    emp.salesRate,
                    emp.note);
        });

        return new ResponseEntity<>(HttpStatus.OK);
    }
}