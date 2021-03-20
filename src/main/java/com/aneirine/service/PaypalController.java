package com.aneirine.service;

import com.aneirine.service.models.OrderData;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;

import static com.aneirine.service.utils.Constants.CANCEL_URL;
import static com.aneirine.service.utils.Constants.SUCCESS_URL;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;

    @GetMapping
    public String home() {
        return "Home";
    }

    @PostMapping
    public String pay(@RequestBody OrderData data) {
        try {
            Payment createdPayment = paypalService.createPayment(data);

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

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return "cancel";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId,
                             @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

}
