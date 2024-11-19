package com.fisherprinting.invoicecommissionservice.customerLevel.controller;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.CustomerInfo;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
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

    @GetMapping("/customerEmployeeAssignedRates")
    public List<CustomerLevelDao.SalesPersonAssignedRateMapper> getSalesPersonAssignedRateMapperList(@RequestParam("customerID") int customerID){
        return this.customerLevelDao.getSalesPersonAssignedRateMapperList(customerID);
    }


    public record SalesAssignedRates(int empId, BigDecimal assignedRate, String salesNote) {}
    public record RateInfo(int customerID, int taskId, BigDecimal taskRate, String taskNote, int lastEditBy, List<SalesAssignedRates> salesAssignedRates) {}
    @PostMapping("/saveCustomerLevelConfig")
    public ResponseEntity<String> saveConfig(@RequestBody List<RateInfo> rateInfoArr) {
//        try {
//            Thread.sleep(3000); // 5000 milliseconds = 5 seconds
//            System.out.println("Done!");
//        } catch (InterruptedException e) {
//            // Handle the interruption if necessary
//            System.err.println("Interrupted: " + e.getMessage());
//        }
        if(customerLevelService.updateConfig(rateInfoArr))
            return new ResponseEntity<String>(HttpStatus.OK);
        return new ResponseEntity<String>(HttpStatus.EXPECTATION_FAILED);
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
