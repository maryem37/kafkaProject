package tn.projectkafka.paymentService.controller;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.projectkafka.paymentService.entity.Payment;
import tn.projectkafka.paymentService.repository.PaymentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payments")
    public List<Payment> getPayments(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<Payment> all = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return all.size() > safeLimit ? all.subList(0, safeLimit) : all;
    }

    @GetMapping("/payments/order/{orderId}")
    public List<Payment> getPaymentsByOrderId(@PathVariable("orderId") Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
