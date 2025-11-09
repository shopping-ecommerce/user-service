package iuh.fit.se.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppealReviewRequest {
    String appealId;
    String status; // APPROVED, REJECTED
    String adminResponse;
}
