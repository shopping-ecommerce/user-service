package iuh.fit.se.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportViolationRequest {
    private String sellerId;
    private String violationType;  // FAKE_PRODUCT, POOR_SERVICE, DELAYED_SHIPPING, etc.
    private String description;
    private List<String> evidenceUrls;  // Link ảnh chứng cứ
}