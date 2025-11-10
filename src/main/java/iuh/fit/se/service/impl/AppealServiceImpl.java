
package iuh.fit.se.service.impl;

import feign.FeignException;
import iuh.fit.event.dto.AppealDecisionEvent;
import iuh.fit.event.dto.AppealSubmittedEvent;
import iuh.fit.se.dto.request.AppealReviewRequest;
import iuh.fit.se.dto.request.DeleteRequest;
import iuh.fit.se.dto.response.*;
import iuh.fit.se.dto.response.enums.Status;
import iuh.fit.se.entity.Appeal;
import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.ViolationRecord;
import iuh.fit.se.entity.enums.AppealStatusEnum;
import iuh.fit.se.entity.enums.SellerStatusEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.AppealMapper;
import iuh.fit.se.mapper.SellerMapper;
import iuh.fit.se.repository.AppealRepository;
import iuh.fit.se.repository.SellerRepository;
import iuh.fit.se.repository.ViolationRecordRepository;
import iuh.fit.se.repository.httpclient.FileClient;
import iuh.fit.se.repository.httpclient.ProductClient;
import iuh.fit.se.service.AppealService;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AppealServiceImpl implements AppealService {

    AppealRepository appealRepository;
    SellerRepository sellerRepository;
    ViolationRecordRepository violationRecordRepository;
    AppealMapper appealMapper;
    SellerMapper sellerMapper;
    FileClient fileClient;
    ProductClient productClient;
    SellerService sellerService;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public AppealResponse submitAppeal(String sellerId, String reason,String productId,
                                       String violationRecordId,
                                       List<MultipartFile> evidences) {

        if(hasAppealed(sellerId, violationRecordId)){
            throw new AppException(ErrorCode.APPEAL_ALREADY_EXISTS);
        }

        // Validate seller
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        // Validate appeal type
//        AppealType type;
//        try {
//            type = AppealType.valueOf(appealType);
//        } catch (IllegalArgumentException e) {
//            throw new AppException(ErrorCode.INVALID_APPEAL_TYPE);
//        }

        // Kiểm tra đã khiếu nại chưa (nếu là về violation)
//        if (type == AppealType.VIOLATION_RECORD && violationRecordId != null) {
//            if (hasAppealed(sellerId, violationRecordId)) {
//                throw new AppException(ErrorCode.APPEAL_ALREADY_EXISTS);
//            }
//
//            // Validate violation exists

//
//            // Kiểm tra thời hạn khiếu nại (7 ngày)
//        }
            ViolationRecord violation = violationRecordRepository.findById(violationRecordId).orElseThrow(() -> new AppException(ErrorCode.VIOLATION_NOT_FOUND));
            if (violation.getReportedAt().plusDays(7).isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.APPEAL_DEADLINE_PASSED);
            }

        // Upload evidences
        List<String> evidenceUrls = new ArrayList<>();
        if (evidences != null && !evidences.isEmpty()) {
            try {
                FileClientResponse response = fileClient.uploadFile(evidences);
                evidenceUrls = response.getResult();
            } catch (FeignException e) {
                log.error("Failed to upload appeal evidences: {}", e.getMessage());
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        // Tạo appeal
        Appeal appeal = Appeal.builder()
                .seller(seller)
                .productId(productId)
                .reason(reason)
                .violationRecordId(violationRecordId)
                .evidenceUrls(evidenceUrls)
                .build();

        Appeal savedAppeal = appealRepository.save(appeal);

        // Gửi notification
//        kafkaTemplate.send("appeal-submitted", AppealSubmittedEvent.builder()
//                .appealId(savedAppeal.getId())
//                .sellerId(sellerId)
//                .sellerEmail(seller.getEmail())
//                .submittedAt(savedAppeal.getSubmittedAt())
//                .build());

        log.info("Appeal submitted: {} by seller: {}", savedAppeal.getId(), sellerId);

        return appealMapper.toResponse(savedAppeal);
    }
    @Override
    public List<AppealResponse> getAppealsBySeller(String sellerId) {
        List<Appeal> appeals = appealRepository.findBySellerIdOrderBySubmittedAtDesc(sellerId);
        return appeals.stream()
                .map(appealMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppealResponse> getPendingAppeals() {
        return appealRepository.findByStatusOrderBySubmittedAtAsc(AppealStatusEnum.PENDING)
                .stream()
                .map(appealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppealResponse> getAllAppeals(String status) {
        if (status == null || status.isEmpty()) {
            return appealRepository.findAllByOrderBySubmittedAtDesc()
                    .stream()
                    .map(appealMapper::toResponse)
                    .collect(Collectors.toList());
        }

        AppealStatusEnum appealStatus = AppealStatusEnum.valueOf(status);
        return appealRepository.findByStatusOrderBySubmittedAtAsc(appealStatus)
                .stream()
                .map(appealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppealDetailResponse getAppealDetail(String appealId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new AppException(ErrorCode.APPEAL_NOT_FOUND));

        Seller seller = appeal.getSeller();

        // Lấy violation record nếu có
        ViolationRecord violationRecord = null;
        if (appeal.getViolationRecordId() != null) {
            violationRecord = violationRecordRepository
                    .findById(appeal.getViolationRecordId())
                    .orElse(null);
        }

        // Đếm số lần khiếu nại trước đó
        int previousAppealsCount = appealRepository.countBySellerIdAndIdNot(
                seller.getId(), appealId);

        return AppealDetailResponse.builder()
                .appeal(appealMapper.toResponse(appeal))
                .seller(sellerMapper.toSellerResponse(seller))
                .violationRecord(violationRecord)
                .violationHistory(seller.getViolationHistory())
                .previousAppealsCount(previousAppealsCount)
                .build();
    }

    @Override
    @Transactional
    public AppealResponse reviewAppeal(AppealReviewRequest request) {
        Appeal appeal = appealRepository.findById(request.getAppealId())
                .orElseThrow(() -> new AppException(ErrorCode.APPEAL_NOT_FOUND));

        if (appeal.getStatus() != AppealStatusEnum.PENDING) {
            throw new AppException(ErrorCode.APPEAL_ALREADY_REVIEWED);
        }

        // Update appeal status
        AppealStatusEnum newStatus = AppealStatusEnum.valueOf(request.getStatus());
        appeal.setStatus(newStatus);
        appeal.setReviewedAt(LocalDateTime.now());
        appeal.setAdminResponse(request.getAdminResponse());
//        appeal.setReviewedBy(request.getAdminId());
        log.info("Appeal {} set to status {} by admin",
                appeal.getId(), newStatus);
        // Xử lý nếu approved
        if (newStatus == AppealStatusEnum.APPROVED) {
            handleApprovedAppeal(appeal);
        }

        Appeal savedAppeal = appealRepository.save(appeal);

        // Gửi notification
        kafkaTemplate.send("appeal-decision", AppealDecisionEvent.builder()
                .appealId(savedAppeal.getId())
                .sellerId(appeal.getSeller().getId())
                .sellerEmail(appeal.getSeller().getEmail())
                .status(newStatus.toString())
                .adminResponse(request.getAdminResponse())
                .reviewedAt(savedAppeal.getReviewedAt())
                .build());

        log.info("Appeal {} reviewed: {}}",
                appeal.getId(), newStatus);

        return appealMapper.toResponse(savedAppeal);
    }

    @Transactional
    public void handleApprovedAppeal(Appeal appeal) {
        log.info("Handling approved appeal: {}", appeal.getId());
        Seller seller = appeal.getSeller();

        // 1) Nếu khiếu nại gắn vi phạm: xoá record & giảm count
        if (appeal.getViolationRecordId() != null && !appeal.getViolationRecordId().isBlank()) {
            handleViolationAppeal(seller, appeal.getViolationRecordId());
        }

        // 2) Nếu khiếu nại về sản phẩm: khôi phục sản phẩm
        if (appeal.getProductId() != null && !appeal.getProductId().isBlank()) {
            handleProductRemovalAppeal(appeal.getProductId());
        }

        // 3) TÁI ĐÁNH GIÁ CHẾ TÀI và ĐẶT LẠI NGÀY HẾT HẠN CHO ĐÚNG NGƯỠNG
        adjustSanctionExactlyAfterAppeal(seller);

        seller.setModifiedTime(LocalDateTime.now());
        sellerRepository.save(seller);

        log.info("Approved appeal handled & sanctions adjusted. appealId={}, sellerId={}",
                appeal.getId(), seller.getId());
    }

    /** Đặt lại đúng khung phạt theo số lỗi hiện tại (exact days from now). */
    private void adjustSanctionExactlyAfterAppeal(Seller seller) {
        int count = java.util.Optional.ofNullable(seller.getViolationCount()).orElse(0);

        // (Tuỳ chính sách nếu vẫn muốn xoá khi >=10 sau tái xét)
//        if (count >= 10) {
//            // TODO: nếu cần, thực hiện xoá tài khoản + thu hồi role + xoá/ẩn sản phẩm.
//            log.warn("Seller {} still >=10 violations after appeal; consider deletion policy.", seller.getId());
//            sellerService.deleteSeller(seller.getId(),"Tài khoản bị xoá do có >=10 vi phạm sau khi khiếu nại.");
//            return;
//        }

        if (count >= 5) {
            setSuspensionExactlyDays(seller, 30, String.format("Sau xét duyệt: %d vi phạm → treo 30 ngày", count));
            return;
        }

        if (count >= 3) {
            // 👉 Trường hợp bạn hỏi: 5 → 4 lỗi → chuyển về đúng 7 ngày
            setSuspensionExactlyDays(seller, 7, String.format("Sau xét duyệt: %d vi phạm → treo 7 ngày", count));
            return;
        }

        // < 3 lỗi → gỡ treo nếu đang treo
        if (seller.getStatus() == SellerStatusEnum.SUSPENDED) {
            try {
                sellerService.unsuspendSeller(seller.getId()); // kích hoạt lại sản phẩm + bắn event
            } catch (Exception e) {
                log.error("Failed to unsuspend after appeal, sellerId={}, err={}", seller.getId(), e.getMessage(), e);
                throw new AppException(ErrorCode.UNSUSPEND_FAILED);
            }
        }
    }

    /** Đặt trạng thái treo và endDate CHÍNH XÁC = now + days (làm tròn 00:00 hôm sau). */
    private void setSuspensionExactlyDays(Seller seller, int days, String reason) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime policyEnd = now.plusDays(days);
        LocalDateTime roundedEnd = policyEnd.toLocalDate().plusDays(1).atStartOfDay();

        // Nếu chưa treo → set treo mới + suspend tất cả SP
        if (seller.getStatus() != SellerStatusEnum.SUSPENDED) {
            seller.setStatus(SellerStatusEnum.SUSPENDED);
            seller.setSuspendedAt(now);
            seller.setSuspensionReason(reason);
            seller.setSuspensionEndDate(roundedEnd);
            try {
                productClient.suspendAllProductsBySeller(seller.getId(), reason);
            } catch (FeignException e) {
                log.error("Suspend products failed for seller {}: {}", seller.getId(), e.contentUTF8());
                throw new AppException(ErrorCode.FEIGN_CLIENT_ERROR);
            }
            return;
        }

        // Đang treo: cập nhật LẠI cho đúng số ngày (có thể rút ngắn từ 30 → 7 hoặc kéo dài lên chuẩn)
        seller.setSuspensionReason(reason);
        seller.setSuspensionEndDate(roundedEnd);
    }


    private void handleViolationAppeal(Seller seller, String violationRecordId) {
        if (violationRecordId == null) {
            log.warn("No violation record ID provided");
            return;
        }

        // Xóa violation khỏi history
        boolean removed = seller.getViolationHistory()
                .removeIf(v -> v.getId().equals(violationRecordId));

        if (removed) {
            // Giảm violation count
            int currentCount = seller.getViolationCount() != null ?
                    seller.getViolationCount() : 0;
            seller.setViolationCount(Math.max(0, currentCount - 1));

            // Xóa violation record
            violationRecordRepository.deleteById(violationRecordId);

            sellerRepository.save(seller);
            log.info("Violation {} removed from seller {}", violationRecordId, seller.getId());
        }
    }

    private void handleProductRemovalAppeal(String productId) {
        if (productId == null) {
            log.warn("No product ID provided");
            return;
        }

        try {
            // Gọi product service để restore product
            productClient.approveProduct(productId, Status.AVAILABLE,"Restored after appeal approval");
            log.info("Product {} restored due to approved appeal", productId);
        } catch (FeignException e) {
            log.error("Failed to restore product {}: {}", productId, e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_RESTORE_FAILED);
        }
    }

    @Override
    public boolean hasAppealed(String sellerId, String violationRecordId) {
        return appealRepository.existsBySellerIdAndViolationRecordId(
                sellerId, violationRecordId);
    }

    @Override
    public int countAppealsBySeller(String sellerId) {
        return appealRepository.countBySellerId(sellerId);
    }
}