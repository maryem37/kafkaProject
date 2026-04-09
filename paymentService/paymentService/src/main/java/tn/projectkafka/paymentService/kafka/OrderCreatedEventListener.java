package tn.projectkafka.paymentService.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tn.projectkafka.paymentService.client.CustomerClient;
import tn.projectkafka.paymentService.entity.Payment;
import tn.projectkafka.paymentService.event.OrderCreatedEvent;
import tn.projectkafka.paymentService.event.PaymentCompletedEvent;
import tn.projectkafka.paymentService.notification.PaymentEmailService;
import tn.projectkafka.paymentService.repository.PaymentRepository;

import java.util.UUID;

@Service
@Slf4j
public class OrderCreatedEventListener {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private final CustomerClient customerClient;
    private final PaymentEmailService paymentEmailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderCreatedEventListener(KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate,
                                    PaymentRepository paymentRepository,
                                    CustomerClient customerClient,
                                    PaymentEmailService paymentEmailService) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentRepository = paymentRepository;
        this.customerClient = customerClient;
        this.paymentEmailService = paymentEmailService;
    }

    @KafkaListener(topics = "order_created_event", groupId = "payment-service")
    public void onOrderCreated(String message) throws Exception {
        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Order created event received for payment: {}", orderCreatedEvent);

        String paymentId = UUID.randomUUID().toString();

        String userEmail = customerClient.getCustomerEmail(orderCreatedEvent.getUserId());

        Payment payment = Payment.builder()
            .paymentId(paymentId)
            .orderId(orderCreatedEvent.getOrderId())
            .userId(orderCreatedEvent.getUserId())
            .userEmail(userEmail)
            .amount(orderCreatedEvent.getTotalPrice())
            .status("COMPLETED")
            .build();

        paymentRepository.saveAndFlush(payment);

        paymentEmailService.sendPaymentCompletedEmail(payment);

        PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
            .paymentId(paymentId)
                .orderId(orderCreatedEvent.getOrderId())
                .userId(orderCreatedEvent.getUserId())
                .userEmail(userEmail)
                .amount(orderCreatedEvent.getTotalPrice())
                .status("COMPLETED")
                .build();

        kafkaTemplate.send("payment_completed_event", paymentCompletedEvent);
        log.info("Payment completed event sent to kafka: {}", paymentCompletedEvent);
    }
}
