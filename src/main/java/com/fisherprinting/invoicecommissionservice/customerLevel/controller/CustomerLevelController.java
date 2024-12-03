package com.fisherprinting.invoicecommissionservice.customerLevel.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.CustomerInfo;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/customerlevel")
public class CustomerLevelController {
    private final CustomerLevelDao customerLevelDao;
    private final CustomerLevelService customerLevelService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public CustomerLevelController(CustomerLevelDao customerLevelDao, CustomerLevelService customerLevelService) {
        this.customerLevelDao = customerLevelDao;
        this.customerLevelService = customerLevelService;
    }

    @GetMapping("/customerListByAr")
    public List<CustomerInfo> getActiveCustomerListById(@RequestParam("arNumber") String arNumber) {
        return customerLevelDao.getActiveCustomerListById(arNumber);
    }

    @GetMapping("/invoiceDepartmentList")
    public List<CustomerLevelDao.TaskDepartment> getTaskDepartmentsList( ){
        return this.customerLevelDao.getTaskDepartmentsList( );
    }

    @GetMapping("/invoiceTaskItemListByDeptId")
    public List<CustomerLevelDao.InvoiceTaskItem>  getInvoiceTaskItemListByDept(@RequestParam("deptId") int deptId) {
        return this.customerLevelService.getInvoiceTaskItemListByDept(deptId);
    }

    @GetMapping("/taskCommissionRates")
    public List<CustomerLevelDao.CustomerLevelTaskRates> getCustomerLevelTaskRatesList(@RequestParam("customerID") int customerID){
        return this.customerLevelDao.getCustomerLevelTaskRatesList(customerID);
    }

    @GetMapping("/employeeAssignedRates")
    public List<CustomerLevelDao.SalesPersonAssignedRateMapper> getSalesPersonAssignedRateMapperList(@RequestParam("customerID") int customerID){
        return this.customerLevelDao.getSalesPersonAssignedRateMapperList(customerID);
    }

//    @GetMapping("/calculatedInvoiceTaskCommission")
//    public CustomerLevelService.CustomerLevelCalculatedCommissionInfo
//    calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
//                                   @RequestParam("invoiceID")int invoiceID,
//                                   @RequestParam("taskID")int taskID,
//                                   @RequestParam("orderNumber")int orderNumber,
//                                   @RequestParam("employeeID")int employeeID) {
//        return customerLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
//    }

//    @GetMapping("/invoiceChargedTaskItems")
//    public List<CustomerLevelDao.InvoiceChargedTaskItem> getInvoiceList(@RequestParam("invoiceId") int invoiceId) {
//        return this.customerLevelDao.getInvoiceChargedItems(invoiceId);
//    }

    @GetMapping("/customerAndJobInfo")
    public CustomerLevelDao.CustomerAndJobInfo getCustomerAndJobInfo(@RequestParam("invoiceId") int invoiceId) {
        return this.customerLevelDao.getCustomerAndJobInfo(invoiceId);
    }

    @GetMapping("/customerInfo")
    public CustomerInfo getCustomerInfo(@RequestParam("invoiceId") int invoiceId) {
        return this.customerLevelService.getCustomerInfoByInvoiceId(invoiceId);
    }

    public record SalesAssignedRates(int empId, BigDecimal assignedRate, String salesNote) {}
    public record RateInfo(
            int customerID,
            int taskId,
            BigDecimal taskRate,
            String taskNote,
            int lastEditBy,
            List<SalesAssignedRates> salesAssignedRates) {}
    @PostMapping("/saveCustomerLevelConfig")
    public ResponseEntity<String> saveCustomerLevelConfig(@RequestBody List<RateInfo> rateInfoArr) {
        if(customerLevelService.updateConfig(rateInfoArr))
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
    }

//    // Currently at Customer Level only
    @GetMapping("/calculatedInvoiceTaskCommission")
    public CustomerLevelService.CustomerLevelCalculatedCommissionInfo
        calculateInvoiceTaskCommission(@RequestParam("customerID")int customerID,
                                       @RequestParam("invoiceID")int invoiceID,
                                       @RequestParam("taskID")int taskID,
                                       @RequestParam("orderNumber")int orderNumber,
                                       @RequestParam("employeeID")int employeeID) {
        return this.customerLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
    }

    @GetMapping("/taskRateInfo")
    public CustomerLevelDao.TaskRateInfo getTaskRateInfo(@RequestParam("customerID") int customerID, @RequestParam("taskID") int taskID) {
        return customerLevelDao.getTaskRateInfo(customerID, taskID);
    }

    @GetMapping("/employeeAssignedRate")
    public CustomerLevelDao.EmployeeTaskRateInfo EmployeeRateInfo(@RequestParam("customerID") int customerID, @RequestParam("empID") int empID, @RequestParam("taskID") int taskID) {
        return customerLevelDao.getEmployeeTaskRateInfo(customerID, empID, taskID);
    }


//    @MessageMapping("/upload")
//    public void handleUpload(@Payload List<Object> jsonArray) {
//        int totalItems = jsonArray.size();
//        int processedItems = 0;
//
//        for (Object item : jsonArray) {
//            // Process the item (e.g., save to database)
//            processedItems++;
//
//            // Send progress update
//            double progress = (double) processedItems / totalItems;
//            messagingTemplate.convertAndSend("/topic/upload-progress", progress);
//        }
//    }

//    @MessageMapping("/upload")
//    public void handleUpload( ) {
//        int totalItems = 200000;
//        int processedItems = 0;
//
//        for(int i = 0; i < totalItems; i++) {
//            processedItems++;
//            double progress = (double) processedItems / totalItems;
//            messagingTemplate.convertAndSend("/topic/upload-progress", progress);
//        }
//    }
//
//    @MessageMapping("/chat")
//    @SendTo("/topic/messages")
//    public String send(String message) throws Exception {
//        return "test";
//    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greet(){
        return "Hello World";
    }

}
