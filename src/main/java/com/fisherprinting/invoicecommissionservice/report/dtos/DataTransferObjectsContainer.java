package com.fisherprinting.invoicecommissionservice.report.dtos;

import com.fisherprinting.invoicecommissionservice.customerLevel.service.CustomerLevelService;
import com.fisherprinting.invoicecommissionservice.invoiceLevel.service.InvoiceLevelService;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public class DataTransferObjectsContainer {
    public record SalesEmployeeCommission(
            CustomerLevelService.CustomerLevelCalculatedCommissionInfo customerLevelCommInfo,
            InvoiceLevelService.InvoiceLevelCalculatedCommissionInfo invoiceLevelCommInfo){ }

    public record FinalSalesCalculatedCommissionInfo(
            String configLevel,
            BigDecimal amount,
            BigDecimal taskRate,
            BigDecimal taskCommissionDollarValue,
            BigDecimal salesPersonAssignedRate,
            BigDecimal salesDollarValue,
            String taskRateNote,
            String salesPersonAssignedRateNote,
            String assignedBy){ }

    public record InvoiceInfo(
            String customerAR,
            int customerId,
            String customerName,
            Date invoiceDate,
            Date paymentDueDate)
    { }
}
