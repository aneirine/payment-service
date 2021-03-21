package com.aneirine.service.api;

import com.aneirine.service.api.models.OrderData;
import com.aneirine.service.entities.PayerEntity;
import com.aneirine.service.entities.PaymentEntity;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.aneirine.service.utils.Constants.CANCEL_URL;
import static com.aneirine.service.utils.Constants.SUCCESS_URL;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final APIContext apiContext;
    private final PaymentRepository paymentRepository;
    private final PayerRepository payerRepository;

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
                createPaymentEntity(payerId, payment);
                return "success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

    private void createPaymentEntity(String payerId, Payment payment) {
        Amount amount = payment.getTransactions().get(0).getAmount();
        Payer payer = payment.getPayer();
        PayerEntity payerEntity = payerRepository.findByPaypalId(payerId);
        System.out.println("PAYER ID " + payerId);
        if (payerEntity == null) {
            PayerInfo payerInfo = payer.getPayerInfo();
            payerEntity = PayerEntity.builder()
                    .countryCode(payerInfo.getCountryCode())
                    .email(payerInfo.getEmail())
                    .firstName(payerInfo.getFirstName())
                    .lastName(payerInfo.getLastName())
                    .paypalId(payerId)
                    .payments(new ArrayList<>())
                    .build();
            payerRepository.save(payerEntity);
        }

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .cart(payment.getCart())
                .currency(amount.getCurrency())
                .payer(payerEntity)
                .total(Double.valueOf(amount.getTotal()))
                .build();

        paymentRepository.save(paymentEntity);
        payerEntity.getPayments().add(paymentEntity);
        payerRepository.save(payerEntity);
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
