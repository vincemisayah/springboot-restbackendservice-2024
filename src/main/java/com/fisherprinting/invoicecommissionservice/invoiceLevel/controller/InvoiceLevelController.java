package com.fisherprinting.invoicecommissionservice.invoiceLevel.controller;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/invoiceLevel")
public class InvoiceLevelController {
    private final InvoiceLevelDao invoiceLevelDao;

    public InvoiceLevelController(InvoiceLevelDao invoiceLevelDao) {
        this.invoiceLevelDao = invoiceLevelDao;
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
        return invoiceLevelDao.getTaskRateInfo(invoiceID, empID, taskID);
    }

    @GetMapping("/invoiceTaskRateInfo")
    public InvoiceLevelDao.EmployeeTaskRateInfo EmployeeRateInfo(@RequestParam("invoiceID") int invoiceID, @RequestParam("empID") int empID, @RequestParam("taskID") int taskID) {
        return invoiceLevelDao.getEmployeeTaskRateInfo(invoiceID, empID, taskID);
    }
}
