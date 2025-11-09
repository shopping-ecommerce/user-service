package iuh.fit.se.controller;

import iuh.fit.event.dto.PolicyEnforceEvent;
import iuh.fit.event.dto.SellerViolationEvent;
import iuh.fit.se.dto.request.ReportViolationRequest;
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
    @KafkaListener(topics = "seller-violations")
    public void handleSellerViolationEvent(SellerViolationEvent event) {
        log.info("Nhận được sự kiện seller-violation: {}", event);
        sellerService.reportViolation(ReportViolationRequest.builder()
                .sellerId(event.getSellerId())
                .violationType("SELLER_TIMEOUT")
                .description(String.format("Seller vi phạm do không xử lý đơn hàng đúng hạn. #%s", event.getOrderId()))
                .build());
    }
}
