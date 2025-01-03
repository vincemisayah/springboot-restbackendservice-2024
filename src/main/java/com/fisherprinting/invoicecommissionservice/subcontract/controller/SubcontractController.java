package com.fisherprinting.invoicecommissionservice.subcontract.controller;

import com.fisherprinting.invoicecommissionservice.subcontract.dao.SubcontractDao;
import com.fisherprinting.invoicecommissionservice.subcontract.dtos.DTOs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/subcontract/v1")
public class SubcontractController {
    private final SubcontractDao subcontractDao;

    public SubcontractController(SubcontractDao subcontractDao) {
        this.subcontractDao = subcontractDao;
    }

    @GetMapping("/linkedToPO")
    public Boolean linkedToPO(@RequestParam("invoiceID") int invoiceID, @RequestParam("taskID") int taskID) {
        return subcontractDao.invoiceIsLinkedToPO(invoiceID, taskID);
    }

    @GetMapping("/chargedTaskItems")
    public List<DTOs.InvoiceChargedTaskItem> getChargedTaskItemsByJobId(@RequestParam("jobId") int jobId) {
        return subcontractDao.getInvoiceTaskItemsByJobId(jobId);
    }

    @GetMapping("/linkedPoItems")
    public DTOs.PoItem getLinkedPurchaseOrderItem(@RequestParam("purchaseOrderItemID") int purchaseOrderItemID){
        return subcontractDao.getLinkedPurchaseOrderItem(purchaseOrderItemID);
    }

    @PostMapping("/linkPoItem")
    public ResponseEntity<String> linkPoItem(@RequestBody DTOs.PoItem poItem){
        try{
            subcontractDao.updatePurchaseOrderItem(poItem);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
