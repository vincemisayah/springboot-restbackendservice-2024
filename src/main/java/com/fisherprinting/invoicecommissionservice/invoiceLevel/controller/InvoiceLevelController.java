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

    @GetMapping("/invoiceChargedTaskItems")
    public List<InvoiceLevelDao.InvoiceChargedTaskItem> getInvoiceList(@RequestParam("invoiceId") int invoiceId) {
        return invoiceLevelDao.getInvoiceChargedItems(invoiceId);
    }

    // Currently at Customer Level only
    @GetMapping("/calculatedInvoiceTaskCommission")
    public InvoiceLevelService.CustomerLevelCalculatedCommissionInfo
    calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
                                   @RequestParam("invoiceID")int invoiceID,
                                   @RequestParam("taskID")int taskID,
                                   @RequestParam("orderNumber")int orderNumber,
                                   @RequestParam("employeeID")int employeeID) {
        return invoiceLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
    }

    public record InvoiceLevelConfig(
            int lastEditedBy,
            int customerID,
            int invoiceID,
            int taskID,
            BigDecimal taskRate,
            BigDecimal salesRate,
            int empID,
            String taskNote,
            String salesEmployeeNote) { }
    @PostMapping("/saveInvoiceLevelConfig")
    public ResponseEntity<String> saveInvoiceLevelConfig(@RequestBody InvoiceLevelConfig invoiceLevelConfig) throws InterruptedException {
        try{
            invoiceLevelDao.saveTaskConfig(invoiceLevelConfig);
            invoiceLevelDao.saveEmployeeConfig(invoiceLevelConfig);
        }catch (DataAccessException e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        TimeUnit.SECONDS.sleep(3);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/invoiceTaskRateInfo")
    public InvoiceLevelDao.TaskRateInfo TaskRateInfo(@RequestParam("invoiceID") int invoiceID, @RequestParam("empID") int empID, @RequestParam("taskID") int taskID) {
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
}