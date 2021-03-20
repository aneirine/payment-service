package com.aneirine.service;

import com.aneirine.service.models.OrderData;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.aneirine.service.utils.Constants.*;

@Service
@RequiredArgsConstructor
public class PaypalService {

    private final APIContext apiContext;


    public Payment createPayment(OrderData data) throws PayPalRESTException {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(CANCEL_URL);
        redirectUrls.setReturnUrl(SUCCESS_URL);

        // Set payment details
        Details details = new Details();
        details.setShipping(String.valueOf(data.getShipping()));
        details.setSubtotal(String.valueOf(data.getSubtotal()));
        details.setTax(String.valueOf(data.getTax()));

        // Payment amount
        Amount amount = new Amount();
        amount.setCurrency("USD");
        // Total must be equal to sum of shipping, tax and subtotal.
        Double total = data.getShipping() + data.getTax() + data.getSubtotal();
        amount.setTotal(String.valueOf(total));
        amount.setDetails(details);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(data.getDescription());

        // Add transaction to a list
        List<Transaction> transactions = Arrays.asList(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent(data.getIntent());
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
