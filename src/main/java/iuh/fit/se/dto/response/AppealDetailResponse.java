package iuh.fit.se.dto.response;

import iuh.fit.se.dto.response.AppealResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.ViolationRecord;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppealDetailResponse {
    // Thông tin appeal
    AppealResponse appeal;

    // Thông tin seller liên quan
    SellerResponse seller;

    // Thông tin vi phạm (nếu có)
    ViolationRecord violationRecord;

    // Thông tin report (nếu có)
//    ReportResponse report;

    // Lịch sử vi phạm của seller
    List<ViolationRecord> violationHistory;

    // Số lần khiếu nại trước đó
    int previousAppealsCount;
}