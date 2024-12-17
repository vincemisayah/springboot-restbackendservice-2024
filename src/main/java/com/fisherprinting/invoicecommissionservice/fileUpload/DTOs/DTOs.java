package com.fisherprinting.invoicecommissionservice.fileUpload.DTOs;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class DTOs {
    public record PaidInvoiceInfo(
      int uploadedBy,
      Timestamp uploadDatetime,
      int invoiceID,
      Date invoiceDate,
      Date datePaid,
      BigDecimal invoiceTotal,
      BigDecimal amountPaid){ }

    public record ViewableFilteredInvoiceData(
            int RowNumber,
            int InvoiceID,
            String InvoiceDate,
            String DatePaid,
            String InvoiceTotal,
            String AmountPaid
    ){ }

    public record InvoiceDup(int invoiceID, int count){ }
    public record Salesperson(int empID, String firstLastName){ }
    public record SalespersonAssignedInvoices(Salesperson salesperson, List<DTOs.ViewableFilteredInvoiceData> assignedInvoices){ }
}
