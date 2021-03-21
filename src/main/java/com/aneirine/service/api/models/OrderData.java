package com.aneirine.service.api.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderData {

    private String currency;
    private String method;
    private PaypalPaymentIntent intent;
    private String description;
    private Double shipping;
    private Double subtotal;
    private Double tax;
    private String cancelUrl;
    private String successUrl;


}
