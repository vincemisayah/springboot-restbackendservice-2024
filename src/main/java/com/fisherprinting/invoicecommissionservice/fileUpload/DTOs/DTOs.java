package com.fisherprinting.invoicecommissionservice.fileUpload.DTOs;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class DTOs {
    public record PaidInvoiceInfo(
      int uploadedBy,
      Timestamp uploadDatetime,
      int invoiceID,
      Date invoiceDate,
      Date datePaid,
      BigDecimal invoiceTotal,
      BigDecimal amountPaid){ }
}
