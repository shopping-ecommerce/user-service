package iuh.fit.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerViolationEvent {
    String sellerId;
    String orderId;
    String method;
    BigDecimal totalPrice;
    String userId;
}
