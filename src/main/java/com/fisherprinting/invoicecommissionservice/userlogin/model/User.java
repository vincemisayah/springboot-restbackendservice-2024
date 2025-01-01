package com.fisherprinting.invoicecommissionservice.userlogin.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;


@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Setter
    @Getter
    private int id;

    @Setter
    @Getter
    private String fullname;

    @Setter
    @Getter
    private String username;

    @Setter
    @Getter
    private String password;
}
