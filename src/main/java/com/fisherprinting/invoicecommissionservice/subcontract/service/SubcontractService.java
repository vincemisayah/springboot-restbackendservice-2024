package com.fisherprinting.invoicecommissionservice.subcontract.service;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;
import com.fisherprinting.invoicecommissionservice.subcontract.dao.SubcontractDao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SubcontractService {
    private final SubcontractDao subcontractDao;
    private final InvoiceLevelDao invoiceLevelDao;
    private final CustomerLevelDao customerLevelDao;
    private final CustomerLevelService customerLevelService;
    private final InvoiceLevelService invoiceLevelService;

    public SubcontractService(SubcontractDao subcontractDao, InvoiceLevelDao invoiceLevelDao, CustomerLevelDao customerLevelDao, CustomerLevelService customerLevelService, InvoiceLevelService invoiceLevelService) {
        this.subcontractDao = subcontractDao;
        this.invoiceLevelDao = invoiceLevelDao;
        this.customerLevelDao = customerLevelDao;
        this.customerLevelService = customerLevelService;
        this.invoiceLevelService = invoiceLevelService;
    }

    public BigDecimal subcontractPercentage(int invoiceID, int taskID){
        BigDecimal poPrice_varA = subcontractDao.invoiceTaskIdPurchaseOrderItemPrice(invoiceID, taskID);
        BigDecimal summedTaskIdsAmount = subcontractDao.getSummedInvoiceTaskIdsAmount(invoiceID, taskID);
        if(summedTaskIdsAmount.equals(new BigDecimal("0.00"))){
            return new BigDecimal(0);
        }

        BigDecimal dividend = new BigDecimal(poPrice_varA.divide(summedTaskIdsAmount, 2, BigDecimal.ROUND_HALF_UP).doubleValue());
        return dividend.multiply(new BigDecimal("100"));
    }

    public record SubcontractLevelCalculatedCommissionInfo(
            BigDecimal amount,
            BigDecimal taskRate,
            BigDecimal taskCommissionDollarValue,
            BigDecimal salesPersonAssignedRate,
            BigDecimal salesDollarValue,
            String taskRateNote,
            String salesPersonAssignedRateNote,
            String assignedBy){ }
    public SubcontractLevelCalculatedCommissionInfo calculateInvoiceTaskCommission(int customerID, int invoiceID, int taskID, int orderNumber, int employeeID){
        // Determine if the invoice ID is linked to a Purchase Order Item.
        // If true, the task rate percentage is calculated based on the Purchase Order Price.
        // The PO Price is matched via the Invoice ID and the Task ID linked by CSRs from the
        // Appserver's PO Item webpage.
        boolean isLinkedToPO = subcontractDao.invoiceIsLinkedToPO(invoiceID, taskID);
        if(!isLinkedToPO){
            return null;
        }


        List<InvoiceLevelDao.InvoiceChargedTaskItem> invoiceChargedTaskItems = invoiceLevelDao.getInvoiceChargedItems(invoiceID);
        invoiceChargedTaskItems.removeIf(n->n.order() != orderNumber);
        InvoiceLevelDao.InvoiceChargedTaskItem invoiceItem = invoiceChargedTaskItems.getFirst();

        CustomerLevelService.CustomerLevelCalculatedCommissionInfo customerLevelCommInfo = this.customerLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
        InvoiceLevelService.InvoiceLevelCalculatedCommissionInfo invoiceLevelCommInfo = this.invoiceLevelService.calculateInvoiceTaskCommission(customerID, invoiceID, taskID, orderNumber, employeeID);
        if(invoiceLevelCommInfo == null && customerLevelCommInfo == null){
            return null;
        }

        // 1 Invoice Task total amount
        BigDecimal amount = invoiceItem.amount();

        // 2 Task Rate Info
        BigDecimal taskRate = subcontractPercentage(invoiceID, taskID);
        // TODO: Include the Purchase Order ID and the Purchase Order Item ID and Price.
        String taskNotes = "Task Note not available. Task Rate is calculated based on the linked Purchase Order Item.";

        // 3 Calculated Task Commission Dollar Value
//        BigDecimal taskCommissionValue = taskRate.multiply(new BigDecimal("100")).divide(invoiceItem.amount());
        BigDecimal taskCommissionValue = invoiceItem.amount().multiply(taskRate.multiply(new BigDecimal("0.01")));

        int scale = 4;
        BigDecimal salesPersonAssignedRate = null;
        BigDecimal salesCommissionValue = null;
        String salesNotes = "";
        if(invoiceLevelCommInfo != null) {
            // 4 Salesperson assigned rate info
            InvoiceLevelDao.EmployeeTaskRateInfo salesPersonAssignedRateInfo = invoiceLevelDao.getEmployeeTaskRateInfo(invoiceID, employeeID, taskID);
            salesPersonAssignedRate = (salesPersonAssignedRateInfo != null ? salesPersonAssignedRateInfo.commRate() : BigDecimal.ZERO);
            salesNotes = (salesPersonAssignedRateInfo != null) ?salesPersonAssignedRateInfo.notes():"";

            // 5 Calculated Sales Commission Dollar Value
            salesCommissionValue = taskCommissionValue.multiply(salesPersonAssignedRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING));
        }

        if(customerLevelCommInfo != null) {
            CustomerLevelDao.EmployeeTaskRateInfo salesPersonAssignedRateInfo = customerLevelDao.getEmployeeTaskRateInfo(customerID, employeeID, taskID);
            salesPersonAssignedRate = salesPersonAssignedRateInfo.commRate();

            // 5 Calculated Sales Commission Dollar Value
            salesCommissionValue = taskCommissionValue.multiply(salesPersonAssignedRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING));
        }


         return new SubcontractLevelCalculatedCommissionInfo(amount.setScale(2, RoundingMode.CEILING),
                taskRate.setScale(2, RoundingMode.CEILING),
                taskCommissionValue.setScale(2, RoundingMode.CEILING),
                salesPersonAssignedRate.setScale(2, RoundingMode.CEILING),
                salesCommissionValue.setScale(2, RoundingMode.CEILING),
                taskNotes,
                salesNotes, "");
    }
}
