package tn.projectkafka.paymentService.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.projectkafka.paymentService.entity.Payment;

@Service
@Slf4j
public class PaymentEmailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String fallbackTo;
    private final String mailUsername;
    private final String mailPassword;

    public PaymentEmailService(JavaMailSender mailSender,
                               @Value("${payment.email.enabled:false}") boolean enabled,
                               @Value("${payment.email.from:no-reply@eventflow.local}") String from,
                               @Value("${payment.email.fallback-to:}") String fallbackTo,
                               @Value("${spring.mail.username:}") String mailUsername,
                               @Value("${spring.mail.password:}") String mailPassword) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.fallbackTo = fallbackTo;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    public void sendPaymentCompletedEmail(Payment payment) {
        if (!enabled || payment == null) {
            return;
        }
        if (mailUsername == null || mailUsername.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            // Avoid noisy failures when credentials are not configured.
            log.info("Payment email enabled but MAILTRAP_USERNAME/MAILTRAP_PASSWORD not set; skipping email for paymentId={}", payment.getPaymentId());
            return;
        }

        String to = (payment.getUserEmail() != null && !payment.getUserEmail().isBlank())
            ? payment.getUserEmail()
            : (fallbackTo != null && !fallbackTo.isBlank() ? fallbackTo : null);
        if (to == null) {
            log.info("No recipient email available; skipping email for paymentId={} (userEmail missing and payment.email.fallback-to empty)", payment.getPaymentId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Payment completed (order " + payment.getOrderId() + ")");
        message.setText(
            "Hello,\n\n" +
            "Your payment has been completed.\n\n" +
            "Payment ID: " + payment.getPaymentId() + "\n" +
            "Order ID: " + payment.getOrderId() + "\n" +
            "Amount: " + payment.getAmount() + "\n" +
            "Status: " + payment.getStatus() + "\n\n" +
            "Thanks,\nEventFlow"
        );

        try {
            mailSender.send(message);
            log.info("Payment email sent to {} for paymentId={}", to, payment.getPaymentId());
        } catch (Exception e) {
            // Intentionally swallow email errors so Kafka flow keeps working.
            log.warn("Failed to send payment email for paymentId={}: {}", payment.getPaymentId(), e.getMessage(), e);
        }
    }
}
