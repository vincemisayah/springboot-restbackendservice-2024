package com.fisherprinting.invoicecommissionservice.customerLevel.model;

public class SalesPerson{
    public Integer salesPersonId;
    public String lastNameFirstName;

    public SalesPerson(Integer id, String name) {
        this.salesPersonId = id;
        this.lastNameFirstName = name;
    }
}
