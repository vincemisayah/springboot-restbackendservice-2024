package com.fisherprinting.invoicecommissionservice.customerLevel.service;

import com.fisherprinting.invoicecommissionservice.customerLevel.controller.CustomerLevelController;
import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.CustomerInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CustomerLevelService {
    private final CustomerLevelDao customerLevelDao;

    public CustomerLevelService(CustomerLevelDao customerLevelDao) {
        this.customerLevelDao = customerLevelDao;
    }

    public List<CustomerLevelDao.InvoiceTaskItem> getInvoiceTaskItemListByDept(int deptId){
        List<CustomerLevelDao.InvoiceTaskItem> list = customerLevelDao.getInvoiceTaskItemList( );
        list.removeIf(n->n.deptId() != deptId);
        return list;
    }

    public Boolean updateConfig(List<CustomerLevelController.RateInfo> rateInfoArr){
        boolean updateFinished = true;
        for(CustomerLevelController.RateInfo rateInfo : rateInfoArr){
            int customerID = rateInfo.customerID();
            int taskID = rateInfo.taskId();
            BigDecimal taskRate = rateInfo.taskRate();
            String taskNote = rateInfo.taskNote();
            int lastEditBy = rateInfo.lastEditBy();

            if(taskRate != null){
                try{
                    customerLevelDao.updateInsertCustomerTaskConfig(customerID, taskID, lastEditBy, taskRate, taskNote);
                }catch (DataAccessException e){
                    System.out.println(e.getMessage());
                    updateFinished = false;
                }

                List<CustomerLevelController.SalesAssignedRates> salesAssignedRates = rateInfo.salesAssignedRates();
                for(CustomerLevelController.SalesAssignedRates sa : salesAssignedRates){
                    try{
                        if(sa.assignedRate() != null)
                            customerLevelDao.updateInsertSalespersonConfig(customerID, sa.empId(), taskID, sa.assignedRate(), lastEditBy, sa.salesNote());
                    }catch (DataAccessException e){
                        System.out.println(e.getMessage());
                        updateFinished = false;
                    }
                }
            }
        }
        return  updateFinished;
    }

    public CustomerInfo getCustomerInfoByInvoiceId(int invoiceId) {
        CustomerLevelDao.CustomerAndJobInfo customerAndJobInfo = customerLevelDao.getCustomerAndJobInfo(invoiceId);
        return customerLevelDao.getSalesPersonListById(customerAndJobInfo.customerID( ));
    }

    public record CustomerLevelCalculatedCommissionInfo(
            BigDecimal amount,
            BigDecimal taskRate,
            BigDecimal taskCommissionDollarValue,
            BigDecimal salesPersonAssignedRate,
            BigDecimal salesDollarValue,
            String taskRateNote,
            String salesPersonAssignedRateNote,
            String assignedBy){ }
    public CustomerLevelCalculatedCommissionInfo calculateInvoiceTaskCommission(int customerID, int invoiceID, int taskID, int orderNumber, int employeeID){
        List<CustomerLevelDao.InvoiceChargedTaskItem> invoiceChargedTaskItems = customerLevelDao.getInvoiceChargedItems(invoiceID);
        invoiceChargedTaskItems.removeIf(n->n.order() != orderNumber);
        CustomerLevelDao.InvoiceChargedTaskItem invoiceItem = invoiceChargedTaskItems.getFirst();

        // 1 Invoice Task total amount
        BigDecimal amount = invoiceItem.amount();

        // 2 Task Rate Info
        CustomerLevelDao.TaskRateInfo taskRateInfo = customerLevelDao.getTaskRateInfo(customerID, employeeID, taskID);
        BigDecimal taskRate = taskRateInfo.commRate();

        // 3 Calculated Task Commission Dollar Value
        int scale = 4;
        BigDecimal rhs = taskRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING);
        BigDecimal taskCommissionValue = amount.multiply(rhs);

        // 4 Salesperson assigned rate info
        CustomerLevelDao.EmployeeTaskRateInfo salesPersonAssignedRateInfo = customerLevelDao.getEmployeeTaskRateInfo(customerID, employeeID, taskID);
        BigDecimal salesPersonAssignedRate = salesPersonAssignedRateInfo.commRate();

        // 5 Calculated Sales Commission Dollar Value
        BigDecimal salesCommissionValue = taskCommissionValue.multiply(salesPersonAssignedRate.divide(new BigDecimal(100), scale, RoundingMode.CEILING));

        return new CustomerLevelCalculatedCommissionInfo(amount.setScale(2, RoundingMode.CEILING),
                taskRate.setScale(2, RoundingMode.CEILING),
                taskCommissionValue.setScale(2, RoundingMode.CEILING),
                salesPersonAssignedRate.setScale(2, RoundingMode.CEILING),
                salesCommissionValue.setScale(2, RoundingMode.CEILING),
                taskRateInfo.notes(),
                salesPersonAssignedRateInfo.notes(),
                taskRateInfo.assignedBy());
    }
}
