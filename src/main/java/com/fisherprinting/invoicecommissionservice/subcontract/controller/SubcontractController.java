package com.fisherprinting.invoicecommissionservice.subcontract.controller;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.controller.InvoiceLevelController;
import com.fisherprinting.invoicecommissionservice.subcontract.dao.SubcontractDao;
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

    @GetMapping("/chargedTaskItems")
    public List<SubcontractDao.InvoiceChargedTaskItem> getChargedTaskItemsByJobId(@RequestParam("jobId") int jobId) {
        return subcontractDao.getInvoiceTaskItemsByJobId(jobId);
    }

    @GetMapping("/linkedPoItems")
    public SubcontractController.PoItem getLinkedPurchaseOrderItem(@RequestParam("purchaseOrderItemID") int purchaseOrderItemID){
        return subcontractDao.getLinkedPurchaseOrderItem(purchaseOrderItemID);
    }

    public record PoItem(int poItemID, int jobID, int invoiceID, int taskID){}
    @PostMapping("/linkPoItem")
    public ResponseEntity<String> linkPoItem(@RequestBody PoItem poItem){
        try{
            subcontractDao.updatePurchaseOrderItem(poItem);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
