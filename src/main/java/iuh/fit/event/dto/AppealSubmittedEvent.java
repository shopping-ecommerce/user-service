package iuh.fit.event.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealSubmittedEvent {
    String appealId;
    String sellerId;
    String sellerEmail;
    String appealType;
    LocalDateTime submittedAt;
}