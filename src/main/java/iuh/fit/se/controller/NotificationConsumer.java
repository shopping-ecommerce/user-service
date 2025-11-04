package iuh.fit.se.controller;

import iuh.fit.event.dto.PolicyEnforceEvent;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    SellerService  sellerService;
    @KafkaListener(topics = "policy-enforce-topic")
    public void handleOrderCreatedEvent(PolicyEnforceEvent event) {
        log.info("Nhận được sự kiện policy-enforce: {}", event);
        try {
            sellerService.suspendAllApproved();
        } catch (Exception e) {
            // TODO: Gửi sự kiện thất bại đến topic "order-failed" nếu cần
        }
    }
}
