package iuh.fit.se.service.impl;

import feign.FeignException;
import iuh.fit.event.dto.SellerVerificationEvent;
import iuh.fit.se.dto.request.AssignRoleRequest;
import iuh.fit.se.dto.request.DeleteRequest;
import iuh.fit.se.dto.request.RevokeRoleRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.AuthClientResponse;
import iuh.fit.se.dto.response.FileClientResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.User;
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
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {
    UserRepository userRepository;
    SellerRepository sellerRepository;
    SellerMapper sellerMapper;
    FileClient fileClient;
    ProductClient productClient;
    AuthClient authClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    @Override
    public SellerResponse createSeller(MultipartFile avatar, List<MultipartFile> identifications, String userId, String shopName, String email, String address) {
        // 1. Kiểm tra User tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem User đã đăng ký làm Seller chưa
        Optional<Seller> existingSeller = sellerRepository.findByUserId(user.getId());
        Seller seller;

        // 3. Upload Hình
        String avatarURL; ;
        List<String> identificationLinks;
        log.error("Avatar: {}, Identifications: {}", avatar.getOriginalFilename(), identifications.stream().map(MultipartFile::getOriginalFilename).toList());
            FileClientResponse avatarResponse = fileClient.uploadFile(List.of(avatar));
            avatarURL = avatarResponse.getResult().get(0);
            FileClientResponse identificationResponse = fileClient.uploadFile(identifications);
            identificationLinks = identificationResponse.getResult();

        if (existingSeller.isPresent()) {
            seller = existingSeller.get();
            // Trường hợp REJECTED: Cập nhật bản ghi hiện có
            seller.setShopName(shopName);
            seller.setAvatarLink(avatarURL);
            seller.setIdentificationLinks(identificationLinks);
            seller.setStatus(SellerStatusEnum.PENDING);
            seller.setRegistrationDate(LocalDateTime.now());
            seller.setModifiedTime(LocalDateTime.now());
            seller.setEmail(email);
            seller.setAddress(address);
        } else {
            // Tạo Seller mới nếu chưa có
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
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND)); // Changed from USER_NOT_FOUND

        // Set seller status
        seller.setStatus(request.getStatus().toUpperCase().equalsIgnoreCase(SellerStatusEnum.APPROVED.toString())
                ? SellerStatusEnum.APPROVED : SellerStatusEnum.REJECTED);

        // Only assign role if status is APPROVED
        if (seller.getStatus() == SellerStatusEnum.APPROVED) {
            log.info("Seller approved, assigning role for user: {}", seller.getUser().getAccountId());

            try {
                AssignRoleRequest assignRoleRequest = AssignRoleRequest.builder()
                        .userId(seller.getUser().getAccountId().toString())
                        .build();

                log.info("Sending role assignment request: {}", assignRoleRequest);
                AuthClientResponse authClientResponse = authClient.assignRoleToUser(assignRoleRequest);
                log.info("Role assignment successful: {}", authClientResponse);

            } catch (FeignException e) {
                log.error("Feign client error while assigning role: Status={}, Message={}",
                        e.status(), e.getMessage());
                throw new AppException(ErrorCode.FEIGN_CLIENT_ERROR);
            } catch (Exception e) {
                log.error("Unexpected error while assigning role: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.FEIGN_CLIENT_ERROR);
            }
        } else {
            log.info("Seller rejected, no role assignment needed for user: {}", seller.getUser().getAccountId());
        }
        List<String> ids = Optional.ofNullable(seller.getIdentificationLinks())
                .map(ArrayList::new)           // copy sang list mutable
                .orElseGet(ArrayList::new);
        // Update modification time
        if (!ids.isEmpty()) {
            try {
                fileClient.deleteByUrl(DeleteRequest.builder().urls(ids).build());
                // Sau khi xóa thành công, set list rỗng nhưng MUTABLE
                seller.setIdentificationLinks(new ArrayList<>());
            } catch (FeignException e) {
                log.error("Delete identifications failed: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.FILE_DELETE_FAILED);
            }
        }
            seller.setModifiedTime(LocalDateTime.now());
            kafkaTemplate.send("seller-verification", SellerVerificationEvent.builder()
                    .sellerId(seller.getId())
                    .sellerEmail(seller.getEmail()) // Assuming Seller has a User with email
                    .status(seller.getStatus().toString())
                    .reason(request.getReason()) // Optional rejection reason
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
                .map(seller -> sellerMapper.toSellerResponse(seller))
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
    }

    @Override
    public SellerResponse updateInfSeller(String sellerId,String shopName, MultipartFile avatar,String address, String email) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(()-> new AppException(ErrorCode.SELLER_NOT_FOUND));
        boolean shouldUpdate = false;

        // Kiểm tra và cập nhật avatar nếu có thay đổi
        if (avatar != null && !avatar.isEmpty()) {
            try {
                fileClient.deleteByUrl(DeleteRequest.builder().urls(List.of(seller.getAvatarLink())).build());
            } catch (FeignException e) {
                log.error("Delete identifications failed: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.FILE_DELETE_FAILED);
            }
            try {
                FileClientResponse fileClientResponse = fileClient.uploadFile(List.of(avatar));
                seller.setAvatarLink(fileClientResponse.getResult().get(0));
                shouldUpdate = true;
            } catch (FeignException e) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        // Kiểm tra và cập nhật shopName nếu có thay đổi
        if (shopName != null && !shopName.trim().isEmpty() && !shopName.trim().equals(seller.getShopName())) {
            seller.setShopName(shopName.trim());
            shouldUpdate = true;
        }
        if(address != null && !address.trim().isEmpty() && !address.trim().equals(seller.getAddress())){
            seller.setAddress(address.trim());
            shouldUpdate = true;
        }

        if(email != null && !email.trim().isEmpty() && !email.trim().equals(seller.getEmail())){
            seller.setEmail(email.trim());
            shouldUpdate = true;
        }

        // Lưu thay đổi nếu có ít nhất một trường được cập nhật
        if (shouldUpdate) {
            seller.setModifiedTime(LocalDateTime.now());
            sellerRepository.save(seller);
        }
        return sellerMapper.toSellerResponse(seller);
    }

    @Override
    public List<SellerResponse> searchSellerPending(){
        List<Seller> sellers = sellerRepository.findAllByStatus(SellerStatusEnum.PENDING);
        return sellers.stream()
                .map(seller -> sellerMapper.toSellerResponse(seller))
                .toList();
    }

    @Override
    public List<SellerResponse> getAllSellers() {
        return sellerRepository.findAll()
                .stream()
                .map(sellerMapper::toSellerResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSellers(List<String> sellerIds) {
        // 1) Không làm gì nếu danh sách rỗng/null
        if (sellerIds == null || sellerIds.isEmpty()) return;

        // 2) Tìm tất cả seller theo danh sách id
        List<Seller> sellers = sellerRepository.findAllById(sellerIds);

        // 3) Kiểm tra thiếu ID nào không
        if (sellers.size() != sellerIds.size()) {
            Set<String> foundIds = sellers.stream().map(Seller::getId).collect(Collectors.toSet());
            List<String> notFound = sellerIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            // Nếu bạn có ErrorCode nhận tham số, có thể truyền danh sách notFound cho rõ ràng
            throw new AppException(ErrorCode.SELLER_NOT_FOUND);
        }

        // 4) Đánh dấu REJECTED cho tất cả seller cần xoá
        sellers.forEach(s -> s.setStatus(SellerStatusEnum.REJECTED));
        sellerRepository.saveAll(sellers);

        // 5) Thu hồi quyền SELLER cho các user liên quan (loại trùng)
        Set<String> userIds = sellers.stream()
                .map(Seller::getUser)          // Seller -> User
                .filter(Objects::nonNull)      // phòng NPE
                .map(User::getAccountId)              // User -> String (userId)
                .filter(Objects::nonNull)      // phòng user chưa có id
                .collect(Collectors.toSet());


        for (String uid : userIds) {
            log.info("Revoking SELLER role for userId: {}", uid);
            try {
                authClient.revokeRoleFromUser(
                        RevokeRoleRequest.builder()
                                .userId(uid)
                                .build()
                );
            } catch (FeignException e){
                log.error("Error revoking SELLER role for userId {}: status={}, body={}",
                        uid, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }
    }

    @Override
    @Transactional
    public void deleteSeller(String sellerId, String reason) {
        // 1) Find the seller by ID
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));

        // 2) Mark the seller as REJECTED
        seller.setStatus(SellerStatusEnum.REJECTED);
        sellerRepository.save(seller);

        // 3) Revoke the SELLER role for the associated user
        User user = seller.getUser();
        if (user != null && user.getAccountId() != null) {
            String userId = user.getAccountId();
            log.info("Revoking SELLER role for userId: {}", userId);
            try {
                productClient.deleteProducts(sellerId, reason);
            } catch (FeignException e){
                log.error("Error deleting products for sellerId {}: status={}, body={}",
                        sellerId, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.SELLER_NOT_FOUND);
            }
            try {
                authClient.revokeRoleFromUser(
                        RevokeRoleRequest.builder()
                                .userId(userId)
                                .build()
                );
            } catch (FeignException e) {
                log.error("Error revoking SELLER role for userId {}: status={}, body={}",
                        userId, e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

        }
    }

    @Override
    public List<String> getAllSellerEmails() {
        return sellerRepository.findAllEmails();
    }

}