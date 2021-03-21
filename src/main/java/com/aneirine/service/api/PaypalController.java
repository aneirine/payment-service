package com.aneirine.service.api;

import com.aneirine.service.api.models.OrderData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.aneirine.service.utils.Constants.CANCEL_URL;
import static com.aneirine.service.utils.Constants.SUCCESS_URL;

@RestController
@RequiredArgsConstructor
public class PaypalController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public String createPayment(@RequestBody OrderData data) {
        return paymentService.handlePayment(data);

    }
    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return paymentService.handleCancelPayment();

    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId,
                             @RequestParam("PayerID") String payerId) {
       return paymentService.handleSuccessPayment(paymentId, payerId);

    }

}
