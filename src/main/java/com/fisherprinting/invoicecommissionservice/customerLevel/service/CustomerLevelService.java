package com.fisherprinting.invoicecommissionservice.customerLevel.service;

import com.fisherprinting.invoicecommissionservice.customerLevel.controller.CustomerLevelController;
import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    // TEST
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
}
