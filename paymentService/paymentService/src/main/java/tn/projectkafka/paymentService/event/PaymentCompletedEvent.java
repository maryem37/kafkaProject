package tn.projectkafka.paymentService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCompletedEvent {

    private String paymentId;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String status;
}
