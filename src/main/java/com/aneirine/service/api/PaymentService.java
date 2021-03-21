package com.aneirine.service.api;

import com.aneirine.service.api.models.OrderData;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.aneirine.service.utils.Constants.CANCEL_URL;
import static com.aneirine.service.utils.Constants.SUCCESS_URL;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final APIContext apiContext;

    @Value("${server.port}")
    private long port;

    private Payment createPayment(OrderData data) throws PayPalRESTException {
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


    private Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        Payment createdPayment = payment.execute(apiContext, paymentExecution);
        return createdPayment;
    }

    public String handleSuccessPayment(String paymentId, String payerId) {
        try {
            Payment payment = executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());

            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

    public String handlePayment(OrderData data) {
        String cancelUrl = "http://localhost:" + port + "/" + CANCEL_URL;
        String successUrl = "http://localhost:" + port + "/" + SUCCESS_URL;
        System.out.println(cancelUrl);
        System.out.println(successUrl);
        data.setCancelUrl(cancelUrl);
        data.setSuccessUrl(successUrl);
        try {
            Payment createdPayment = createPayment(data);

            Iterator links = createdPayment.getLinks().iterator();
            while (links.hasNext()) {
                Links link = (Links) links.next();
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            System.err.println(e.getDetails());
        }
        return "redirect:/";
    }

    public String handleCancelPayment() {

        return "cancel";
    }
}
