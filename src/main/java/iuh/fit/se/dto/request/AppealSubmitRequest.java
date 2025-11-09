package iuh.fit.se.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppealSubmitRequest {
    String sellerId;
    String appealType; // VIOLATION_RECORD, SUSPENSION, PRODUCT_REMOVAL, REPORT_DISPUTE
    String reason;
    String violationRecordId; // Optional - nếu khiếu nại về vi phạm cụ thể
    String reportId; // Optional - nếu khiếu nại về report
    String productId; // Optional - nếu khiếu nại về sản phẩm bị xóa
}