package tn.projectkafka.paymentService.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CustomerClient(RestTemplate restTemplate,
                          @Value("${customer.service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String getCustomerEmail(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            CustomerDto dto = restTemplate.getForObject(baseUrl + "/customers/" + userId, CustomerDto.class);
            return dto != null ? dto.email : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class CustomerDto {
        public Long id;
        public String name;
        public String email;
        public String address;
    }
}