package iuh.fit.se.service.impl;

import feign.FeignException;
import iuh.fit.event.dto.SellerSuspensionEvent;
import iuh.fit.event.dto.SellerUnsuspensionEvent;
import iuh.fit.event.dto.SellerVerificationEvent;
import iuh.fit.event.dto.SellerWarningEvent;
import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.AuthClientResponse;
import iuh.fit.se.dto.response.FileClientResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.User;
import iuh.fit.se.entity.ViolationRecord;
import iuh.fit.se.entity.enums.SellerStatusEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.SellerMapper;
import iuh.fit.se.repository.SellerRepository;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.repository.httpclient.AuthClient;
import iuh.fit.se.repository.httpclient.FileClient;
import iuh.fit.se.repository.httpclient.ProductClient;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {

    private static final int VIOLATION_LEVEL_1 = 3;   // 7 ngày
    private static final int VIOLATION_LEVEL_2 = 5;   // 30 ngày
    private static final int VIOLATION_LEVEL_3 = 10;  // 6 tháng

    UserRepository userRepository;
    SellerRepository sellerRepository;
    SellerMapper sellerMapper;
    FileClient fileClient;
    ProductClient productClient;
    AuthClient authClient;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public SellerResponse createSeller(MultipartFile avatar, List<MultipartFile> identifications, String userId, String shopName, String email, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Optional<Seller> existingSeller = sellerRepository.findByUserId(user.getId());
        Seller seller;

        String avatarURL;
        List<String> identificationLinks;

        log.error("Avatar: {}, Identifications: {}", avatar.getOriginalFilename(),
                identifications.stream().map(MultipartFile::getOriginalFilename).toList());

        FileClientResponse avatarResponse = fileClient.uploadFile(List.of(avatar));
        avatarURL = avatarResponse.getResult().get(0);

        FileClientResponse identificationResponse = fileClient.uploadFile(identifications);
        identificationLinks = identificationResponse.getResult();

        if (existingSeller.isPresent()) {
            seller = existingSeller.get();
            seller.setShopName(shopName);
            seller.setAvatarLink(avatarURL);
            seller.setIdentificationLinks(identificationLinks);
            seller.setStatus(SellerStatusEnum.PENDING);
            seller.setRegistrationDate(LocalDateTime.now());
            seller.setModifiedTime(LocalDateTime.now());
            seller.setEmail(email);
            seller.setAddress(address);
        } else {
            seller = Seller.builder()
                    .user(user)
                    .shopName(shopName)
                    .avatarLink(avatarURL)
                    .email(email)
                    .address(address)
                    .identificationLinks(identificationLinks)
                    .build();
        }
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse verifySeller(SellerVerifyRequest request) {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        // So sánh status gọn gàng, tránh toUpperCase().equalsIgnoreCase(...)
        boolean approved = SellerStatusEnum.APPROVED.name().equalsIgnoreCase(request.getStatus());
        seller.setStatus(approved ? SellerStatusEnum.APPROVED : SellerStatusEnum.REJECTED);

        if (seller.getStatus() == SellerStatusEnum.APPROVED) {
            log.info("Seller approved, assigning role for user: {}", seller.getUser().getAccountId());
            try {
                AssignRoleRequest assignRoleRequest = AssignRoleRequest.builder()
                        .userId(String.valueOf(seller.getUser().getAccountId()))
                        .build();
                AuthClientResponse authClientResponse = authClient.assignRoleToUser(assignRoleRequest);
                log.info("Role assignment successful: {}", authClientResponse);
            } catch (FeignException e) {
                log.error("Feign client error while assigning role: Status={}, Message={}", e.status(), e.getMessage());
                throw new AppException(ErrorCode.FEIGN_CLIENT_ERROR);
            } catch (Exception e) {
                log.error("Unexpected error while assigning role: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.FEIGN_CLIENT_ERROR);
            }
        } else {
            log.info("Seller rejected, no role assignment needed for user: {}", seller.getUser().getAccountId());
        }

        List<String> ids = Optional.ofNullable(seller.getIdentificationLinks()).map(ArrayList::new).orElseGet(ArrayList::new);
        if (!ids.isEmpty()) {
            try {
                fileClient.deleteByUrl(DeleteRequest.builder().urls(ids).build());
                seller.setIdentificationLinks(new ArrayList<>());
            } catch (FeignException e) {
                log.error("Delete identifications failed: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.FILE_DELETE_FAILED);
            }
        }

        seller.setModifiedTime(LocalDateTime.now());
        kafkaTemplate.send("seller-verification", SellerVerificationEvent.builder()
                .sellerId(seller.getId())
                .sellerEmail(seller.getEmail())
                .status(seller.getStatus().toString())
                .reason(request.getReason())
                .build()
        );
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse searchByUserId(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
        return sellerMapper.toSellerResponse(seller);
    }

    @Override
    public SellerResponse searchBySellerId(String sellerId) {
        return sellerRepository.findById(sellerId)
                .map(sellerMapper::toSellerResponse)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
    }

    @Override
    public SellerResponse updateInfSeller(String sellerId, String shopName, MultipartFile avatar, String address, String email) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
        boolean shouldUpdate = false;

        if (avatar != null && !avatar.isEmpty()) {
            try {
                if (seller.getAvatarLink() != null) {
                    fileClient.deleteByUrl(DeleteRequest.builder().urls(List.of(seller.getAvatarLink())).build());
                }
                FileClientResponse fileClientResponse = fileClient.uploadFile(List.of(avatar));
                seller.setAvatarLink(fileClientResponse.getResult().get(0));
                shouldUpdate = true;
            } catch (FeignException e) {
                log.error("Avatar update failed: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        if (shopName != null && !shopName.trim().isEmpty() && !shopName.trim().equals(seller.getShopName())) {
            seller.setShopName(shopName.trim());
            shouldUpdate = true;
        }
        if (address != null && !address.trim().isEmpty() && !address.trim().equals(seller.getAddress())) {
            seller.setAddress(address.trim());
            shouldUpdate = true;
        }
        if (email != null && !email.trim().isEmpty() && !email.trim().equals(seller.getEmail())) {
            seller.setEmail(email.trim());
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            seller.setModifiedTime(LocalDateTime.now());
            sellerRepository.save(seller);
        }
        return sellerMapper.toSellerResponse(seller);
    }

    @Override
    public List<SellerResponse> searchSellerPending() {
        return sellerRepository.findAllByStatus(SellerStatusEnum.PENDING)
                .stream().map(sellerMapper::toSellerResponse).toList();
    }

    @Override
    public List<SellerResponse> searchSellerApproved() {
        return sellerRepository.findAllByStatus(SellerStatusEnum.APPROVED)
                .stream().map(sellerMapper::toSellerResponse).toList();
    }

    @Override
    public List<SellerResponse> getAllSellers() {
        return sellerRepository.findAll().stream().map(sellerMapper::toSellerResponse).toList();
    }

    @Override
    @Transactional
    public void deleteSellers(List<String> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) return;

        List<Seller> sellers = sellerRepository.findAllById(sellerIds);

        if (sellers.size() != sellerIds.size()) {
            Set<String> foundIds = sellers.stream().map(Seller::getId).collect(Collectors.toSet());
            List<String> notFound = sellerIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new AppException(ErrorCode.SELLER_NOT_FOUND);
        }

        sellers.forEach(s -> s.setStatus(SellerStatusEnum.REJECTED));
        sellerRepository.saveAll(sellers);

        Set<String> userIds = sellers.stream()
                .map(Seller::getUser)
                .filter(Objects::nonNull)
                .map(User::getAccountId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String uid : userIds) {
            log.info("Revoking SELLER role for userId: {}", uid);
            try {
                authClient.revokeRoleFromUser(RevokeRoleRequest.builder().userId(uid).build());
            } catch (FeignException e) {
                log.error("Error revoking SELLER role for userId {}: status={}, body={}", uid, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }
    }

    @Override
    @Transactional
    public void deleteSeller(String sellerId, String reason) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        seller.setStatus(SellerStatusEnum.REJECTED);
        sellerRepository.save(seller);

        User user = seller.getUser();
        if (user != null && user.getAccountId() != null) {
            String userId = user.getAccountId();
            log.info("Revoking SELLER role for userId: {}", userId);
            try {
                productClient.deleteProducts(sellerId, reason);
            } catch (FeignException e) {
                log.error("Error deleting products for sellerId {}: status={}, body={}", sellerId, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.SELLER_NOT_FOUND);
            }
            try {
                authClient.revokeRoleFromUser(RevokeRoleRequest.builder().userId(userId).build());
            } catch (FeignException e) {
                log.error("Error revoking SELLER role for userId {}: status={}, body={}", userId, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }
    }

    @Override
    public List<String> getAllSellerEmails() {
        return sellerRepository.findAllEmailsByStatus();
    }

    @Override
    @Transactional
    public SellerResponse reportViolation(ReportViolationRequest request) {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        if (seller.getStatus() != SellerStatusEnum.APPROVED) {
            throw new AppException(ErrorCode.SELLER_NOT_APPROVED);
        }

        // null-safe
        int currentViolations = Optional.ofNullable(seller.getViolationCount()).orElse(0) + 1;
        seller.setViolationCount(currentViolations);

        ViolationRecord violationRecord = ViolationRecord.builder()
                .seller(seller) // QUAN TRỌNG
                .violationType(request.getViolationType())
                .description(request.getDescription())
                .evidenceUrls(request.getEvidenceUrls())
                .reportedAt(LocalDateTime.now())
                .violationNumber(currentViolations)
                .build();

        if (seller.getViolationHistory() == null) {
            seller.setViolationHistory(new ArrayList<>());
        }
        seller.getViolationHistory().add(violationRecord);

        SuspensionInfo suspensionInfo = determineSuspensionPeriod(currentViolations);

        if (suspensionInfo != null) {
            seller.setStatus(SellerStatusEnum.SUSPENDED);
            seller.setSuspensionReason(suspensionInfo.getReason());
            seller.setSuspendedAt(LocalDateTime.now());
            // Làm tròn suspensionEndDate lên 00:00 ngày hôm sau
            LocalDateTime endDate = suspensionInfo.getEndDate();
            LocalDateTime roundedEndDate = endDate.toLocalDate().plusDays(1).atStartOfDay();
            seller.setSuspensionEndDate(roundedEndDate);

            productClient.suspendAllProductsBySeller(seller.getId(), suspensionInfo.getReason());
            kafkaTemplate.send("seller-suspension", SellerSuspensionEvent.builder()
                    .sellerId(seller.getId())
                    .sellerEmail(seller.getEmail())
                    .violationType(request.getViolationType())
                    .violationCount(currentViolations)
                    .reason(suspensionInfo.getReason())
                    .suspensionEndDate(suspensionInfo.getEndDate())
                    .suspensionDays(suspensionInfo.getDays())
                    .build());

            log.warn("Seller {} suspended for {} days. Violation count: {}", seller.getId(), suspensionInfo.getDays(), currentViolations);
        } else {
            kafkaTemplate.send("seller-warning", SellerWarningEvent.builder()
                    .sellerId(seller.getId())
                    .sellerEmail(seller.getEmail())
                    .violationType(request.getViolationType())
                    .violationCount(currentViolations)
                    .warningMessage(String.format("Cảnh báo vi phạm lần %d. Nếu vi phạm %d lần sẽ bị tạm giam tài khoản.", currentViolations, VIOLATION_LEVEL_1))
                    .build());

            log.info("Warning issued to seller {}. Violation count: {}", seller.getId(), currentViolations);
        }

        seller.setModifiedTime(LocalDateTime.now());
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    private SuspensionInfo determineSuspensionPeriod(int violationCount) {
        LocalDateTime now = LocalDateTime.now();
        if (violationCount >= VIOLATION_LEVEL_3) {
            return SuspensionInfo.builder().days(180).endDate(now.plusMonths(6))
                    .reason(String.format("Vi phạm nghiêm trọng lần thứ %d. Tạm giam 6 tháng.", violationCount)).build();
        } else if (violationCount >= VIOLATION_LEVEL_2) {
            return SuspensionInfo.builder().days(30).endDate(now.plusDays(30))
                    .reason(String.format("Vi phạm lần thứ %d. Tạm giam 30 ngày.", violationCount)).build();
        } else if (violationCount >= VIOLATION_LEVEL_1) {
            return SuspensionInfo.builder().days(7).endDate(now.plusDays(7))
                    .reason(String.format("Vi phạm lần thứ %d. Tạm giam 7 ngày.", violationCount)).build();
        }
        return null;
    }

    @Override
    @Transactional
    public SellerResponse unsuspendSeller(String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        if (seller.getStatus() != SellerStatusEnum.SUSPENDED) {
            throw new AppException(ErrorCode.SELLER_NOT_SUSPENDED);
        }

        seller.setStatus(SellerStatusEnum.APPROVED);
        seller.setSuspensionReason(null);
        seller.setSuspendedAt(null);
        seller.setSuspensionEndDate(null);
        seller.setModifiedTime(LocalDateTime.now());
        productClient.activateAllProductsBySeller(seller.getId());
        kafkaTemplate.send("seller-unsuspension", SellerUnsuspensionEvent.builder()
                .sellerId(seller.getId())
                .sellerEmail(seller.getEmail())
                .unsuspendedAt(LocalDateTime.now())
                .build());
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    @Transactional
    public void autoUnsuspendExpiredSellers() {
        LocalDateTime now = LocalDateTime.now();
        List<Seller> expiredSuspensions =
                sellerRepository.findByStatusAndSuspensionEndDateBefore(SellerStatusEnum.SUSPENDED, now);

        log.info("Found {} sellers with expired suspension", expiredSuspensions.size());

        expiredSuspensions.forEach(seller -> {
            try {
                unsuspendSeller(seller.getId());
                log.info("Auto-unsuspended seller: {}", seller.getId());
            } catch (Exception e) {
                log.error("Error auto-unsuspending seller {}: {}", seller.getId(), e.getMessage());
            }
        });
    }

    @Override
    public List<SellerResponse> getAllSellerStatusSuspended() {
        return sellerRepository.findAllByStatus(SellerStatusEnum.SUSPENDED)
                .stream().map(sellerMapper::toSellerResponse).toList();
    }

    @Override
    public List<ViolationRecord> getViolationHistory(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND))
                .getViolationHistory();
    }
    @Override
    @Transactional
    public int suspendAllApproved() {
        int affected = sellerRepository.bulkSuspendAllApprovedJpql(
                "Enforce policy compliance - automatic suspension"
        );
        log.warn("Bulk update (status only): {} seller APPROVED -> SUSPENDED", affected);
        return affected;
    }

    @Override
    @Transactional
    public int resetMonthlyViolationCounters() {
        int affected = sellerRepository.resetAllViolationCountToZero();
        log.info("Monthly violation reset executed, rows affected = {}", affected);
        return affected;
    }

    @Data
    @Builder
    private static class SuspensionInfo {
        private int days;
        private LocalDateTime endDate;
        private String reason;
    }
}
