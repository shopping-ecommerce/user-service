
package iuh.fit.se.service;

import iuh.fit.se.dto.request.AppealReviewRequest;
import iuh.fit.se.dto.request.AppealSubmitRequest;
import iuh.fit.se.dto.response.AppealDetailResponse;
import iuh.fit.se.dto.response.AppealResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppealService {

    /**
     * Seller tạo khiếu nại mới
     */
    AppealResponse submitAppeal(String sellerId, String reason,String productId,
                                String violationRecordId,
                                List<MultipartFile> evidences);

    /**
     * Lấy danh sách khiếu nại của seller
     */
    List<AppealResponse> getAppealsBySeller(String sellerId);

    /**
     * Admin lấy tất cả khiếu nại pending
     */
    List<AppealResponse> getPendingAppeals();

    /**
     * Admin lấy tất cả khiếu nại (có filter)
     */
    List<AppealResponse> getAllAppeals(String status);

    /**
     * Admin xem chi tiết khiếu nại
     */
    AppealDetailResponse getAppealDetail(String appealId);

    /**
     * Admin xử lý khiếu nại
     */
    AppealResponse reviewAppeal(AppealReviewRequest request);

    /**
     * Kiểm tra seller đã khiếu nại vi phạm này chưa
     */
    boolean hasAppealed(String sellerId, String violationRecordId);

    /**
     * Đếm số lần khiếu nại của seller
     */
    int countAppealsBySeller(String sellerId);
}