package com.aneirine.service.models;

import lombok.Getter;

@Getter
public class OrderData {

    private String currency;
    private String method;
    private String intent;
    private String description;
    private Double shipping;
    private Double subtotal;
    private Double tax;


}
