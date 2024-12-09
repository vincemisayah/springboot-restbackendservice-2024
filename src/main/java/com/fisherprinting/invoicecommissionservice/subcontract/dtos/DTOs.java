package com.fisherprinting.invoicecommissionservice.subcontract.dtos;

import java.math.BigDecimal;

public class DTOs {
    public record PoItem(int poItemID, int jobID, int invoiceID, int taskID){ }

    public record InvoiceChargedTaskItem(
            int invoice,
            int order,
            int deptId,
            String deptName,
            int taskId,
            String taskName,
            String description,
            Double qty,
            BigDecimal cost,
            BigDecimal amount
    ) { }
}
