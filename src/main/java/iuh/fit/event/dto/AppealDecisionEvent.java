package iuh.fit.event.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealDecisionEvent {
    String appealId;
    String sellerId;
    String sellerEmail;
    String appealType;
    String status;
    String adminResponse;
    LocalDateTime reviewedAt;
}