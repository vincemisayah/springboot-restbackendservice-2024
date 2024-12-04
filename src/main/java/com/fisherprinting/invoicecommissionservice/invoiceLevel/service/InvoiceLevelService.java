package com.fisherprinting.invoicecommissionservice.invoiceLevel.service;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InvoiceLevelService {
    private final InvoiceLevelDao invoiceLevelDao;

    public InvoiceLevelService(InvoiceLevelDao invoiceLevelDao) {
        this.invoiceLevelDao = invoiceLevelDao;
    }

    public record InvoiceLevelCalculatedCommissionInfo(
            BigDecimal amount,
            BigDecimal taskRate,
            BigDecimal taskCommissionDollarValue,
            BigDecimal salesPersonAssignedRate,
            BigDecimal salesDollarValue,
            String taskRateNote,
            String salesPersonAssignedRateNote,
            String assignedBy){ }
    public InvoiceLevelCalculatedCommissionInfo calculateInvoiceTaskCommission(int customerID, int invoiceID, int taskID, int orderNumber, int employeeID){
        List<InvoiceLevelDao.InvoiceChargedTaskItem> invoiceChargedTaskItems = invoiceLevelDao.getInvoiceChargedItems(invoiceID);
        invoiceChargedTaskItems.removeIf(n->n.order() != orderNumber);
        InvoiceLevelDao.InvoiceChargedTaskItem invoiceItem = invoiceChargedTaskItems.getFirst();

        // 1 Invoice Task total amount
        BigDecimal amount = invoiceItem.amount();

        // 2 Task Rate Info
        InvoiceLevelDao.TaskRateInfo taskRateInfo = invoiceLevelDao.getTaskRateInfo(invoiceID, taskID);
        if(!taskRateInfo.active()) return null; // Return null if task config is disabled.
        BigDecimal taskRate = (taskRateInfo != null)? taskRateInfo.commRate(): BigDecimal.ZERO;
        String taskNotes =(taskRateInfo != null) ? taskRateInfo.notes():"";

        // 3 Calculated Task Commission Dollar Value
        int scale = 4;
        BigDecimal rhs = taskRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING);
        BigDecimal taskCommissionValue = amount.multiply(rhs);

        // 4 Salesperson assigned rate info
        InvoiceLevelDao.EmployeeTaskRateInfo salesPersonAssignedRateInfo = invoiceLevelDao.getEmployeeTaskRateInfo(invoiceID, employeeID, taskID);
        BigDecimal salesPersonAssignedRate = (salesPersonAssignedRateInfo != null ? salesPersonAssignedRateInfo.commRate() : BigDecimal.ZERO);
        String salesNotes =(salesPersonAssignedRateInfo != null) ?salesPersonAssignedRateInfo.notes():"";

        // 5 Calculated Sales Commission Dollar Value
        BigDecimal salesCommissionValue = taskCommissionValue.multiply(salesPersonAssignedRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING));



        return new InvoiceLevelCalculatedCommissionInfo(amount.setScale(2, RoundingMode.CEILING),
                taskRate.setScale(2, RoundingMode.CEILING),
                taskCommissionValue.setScale(2, RoundingMode.CEILING),
                salesPersonAssignedRate.setScale(2, RoundingMode.CEILING),
                salesCommissionValue.setScale(2, RoundingMode.CEILING),
                taskRateInfo.notes(),
                salesNotes,
                taskRateInfo.assignedBy());
    }
}
