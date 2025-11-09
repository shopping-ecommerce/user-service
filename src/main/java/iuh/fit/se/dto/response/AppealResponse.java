
package iuh.fit.se.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppealResponse {
    String id;
    String sellerId;
    String sellerName;
    String sellerEmail;
    String reason;
    String violationRecordId;
    String productId;
    List<String> evidenceUrls;
    String status;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
    String adminResponse;
}
