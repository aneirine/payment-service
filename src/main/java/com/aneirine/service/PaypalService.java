package com.aneirine.service;

import com.aneirine.service.models.OrderData;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaypalService {

    private final APIContext apiContext;


    public Payment createPayment(OrderData data) throws PayPalRESTException {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(data.getCancelUrl());
        redirectUrls.setReturnUrl(data.getSuccessUrl());

        Details details = new Details();
        details.setShipping(String.valueOf(data.getShipping()));
        details.setSubtotal(String.valueOf(data.getSubtotal()));
        details.setTax(String.valueOf(data.getTax()));

        // Payment amount
        Amount amount = new Amount();
        amount.setCurrency("USD");
        // Total must be equal to sum of shipping, tax and subtotal.
        Double total = data.getShipping() + data.getTax() + data.getSubtotal();
        total = new BigDecimal(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
        String totalStr = String.valueOf(total);
        amount.setTotal(totalStr);
        amount.setDetails(details);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(data.getDescription());

        // Add transaction to a list
        List<Transaction> transactions = Arrays.asList(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent(data.getIntent().toString().toLowerCase());
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);

        return payment.create(apiContext);
    }


    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        Payment createdPayment = payment.execute(apiContext, paymentExecution);
        return createdPayment;
    }
}
