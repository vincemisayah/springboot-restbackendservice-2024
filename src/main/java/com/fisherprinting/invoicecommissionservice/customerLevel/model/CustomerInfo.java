package com.fisherprinting.invoicecommissionservice.customerLevel.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CustomerInfo {
    private int id;
    private String name;
    private String arNumber;
    private List<SalesPerson> salesPersonList;

    public CustomerInfo( ) {
        super( );
    }

    public CustomerInfo(int id, String name, String arNumber2 ,List<SalesPerson> salesPersonList) {
        super();
        this.id = id;
        this.name = name;
        this.arNumber = arNumber2;
        this.salesPersonList = salesPersonList;
    }

}
