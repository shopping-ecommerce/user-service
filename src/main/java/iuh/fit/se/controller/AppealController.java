package iuh.fit.se.controller;

import iuh.fit.se.dto.request.AppealReviewRequest;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.AppealDetailResponse;
import iuh.fit.se.dto.response.AppealResponse;
import iuh.fit.se.service.AppealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
@Slf4j
public class AppealController {

    private final AppealService appealService;

    // Seller tạo khiếu nại
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/submit")
    public ApiResponse<AppealResponse> submitAppeal(
            @RequestPart("evidences") List<MultipartFile> evidences,
            @RequestParam("sellerId") String sellerId,
            @RequestParam("reason") String reason,
            @RequestParam("productId") String productId,
            @RequestParam(value = "violationRecordId", required = false) String violationRecordId) {
        return ApiResponse.<AppealResponse>builder()
                .code(200)
                .message("Appeal submitted successfully")
                .result(appealService.submitAppeal(sellerId, reason,productId,
                        violationRecordId, evidences))
                .build();
    }

    // Seller xem khiếu nại của mình
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<AppealResponse>> getSellerAppeals(
            @PathVariable String sellerId) {
        return ApiResponse.<List<AppealResponse>>builder()
                .code(200)
                .result(appealService.getAppealsBySeller(sellerId))
                .build();
    }

    // Admin xem tất cả khiếu nại pending
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ApiResponse<List<AppealResponse>> getPendingAppeals() {
        return ApiResponse.<List<AppealResponse>>builder()
                .code(200)
                .result(appealService.getPendingAppeals())
                .build();
    }

    // Admin xử lý khiếu nại
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/review")
    public ApiResponse<AppealResponse> reviewAppeal(
            @RequestBody AppealReviewRequest request) {
        return ApiResponse.<AppealResponse>builder()
                .code(200)
                .message("Appeal reviewed successfully")
                .result(appealService.reviewAppeal(request))
                .build();
    }

    // Admin xem chi tiết khiếu nại
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{appealId}")
    public ApiResponse<AppealDetailResponse> getAppealDetail(
            @PathVariable String appealId) {
        return ApiResponse.<AppealDetailResponse>builder()
                .code(200)
                .result(appealService.getAppealDetail(appealId))
                .build();
    }
}