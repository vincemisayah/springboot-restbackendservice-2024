package com.fisherprinting.invoicecommissionservice.invoiceLevel.service;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import org.springframework.stereotype.Service;

@Service
public class InvoiceLevelService {
    private final InvoiceLevelDao invoiceLevelDao;

    public InvoiceLevelService(InvoiceLevelDao invoiceLevelDao) {
        this.invoiceLevelDao = invoiceLevelDao;
    }
}
