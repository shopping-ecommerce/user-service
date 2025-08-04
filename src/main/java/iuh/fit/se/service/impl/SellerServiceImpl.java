package iuh.fit.se.service.impl;

import feign.FeignException;
import iuh.fit.se.dto.request.AssignRoleRequest;
import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.AuthClientResponse;
import iuh.fit.se.dto.response.FileClientResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.User;
import iuh.fit.se.enums.SellerStatusEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.SellerMapper;
import iuh.fit.se.repository.SellerRepository;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.repository.httpclient.AuthClient;
import iuh.fit.se.repository.httpclient.FileClient;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {
    UserRepository userRepository;
    SellerRepository sellerRepository;
    SellerMapper sellerMapper;
    FileClient fileClient;
    AuthClient authClient;

    @Override
    public SellerResponse createSeller(MultipartFile avatar, List<MultipartFile> identifications, String userId, String shopName) {
        // 1. Kiểm tra User tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem User đã đăng ký làm Seller chưa
        Optional<Seller> existingSeller = sellerRepository.findByUserId(user.getId());
        Seller seller;

        // 3. Upload Hình
        String avatarURL; ;
        List<String> identificationLinks;
        try {
            FileClientResponse avatarResponse = fileClient.uploadFile(List.of(avatar));
            avatarURL = avatarResponse.getResult().get(0);
            FileClientResponse identificationResponse = fileClient.uploadFile(identifications);
            identificationLinks = identificationResponse.getResult();
        } catch (FeignException e){
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
        if (existingSeller.isPresent()) {
            seller = existingSeller.get();
            // Trường hợp REJECTED: Cập nhật bản ghi hiện có
            seller.setShopName(shopName);
            seller.setAvatarLink(avatarURL);
            seller.setIdentificationLinks(identificationLinks);
            seller.setStatus(SellerStatusEnum.PENDING);
            seller.setRegistrationDate(LocalDateTime.now());
            seller.setModifiedTime(LocalDateTime.now());
        } else {
            // Tạo Seller mới nếu chưa có
            seller = Seller.builder()
                    .user(user)
                    .shopName(shopName)
                    .avatarLink(avatarURL)
                    .identificationLinks(identificationLinks)
                    .build();
        }
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse verifySeller(SellerVerifyRequest request) {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(()-> new AppException(ErrorCode.SELLER_NOT_FOUND)); // Changed from USER_NOT_FOUND

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

        // Update modification time
        seller.setModifiedTime(LocalDateTime.now());

        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse searchByUserId(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
        return sellerMapper.toSellerResponse(seller);
    }

    @Override
    public SellerResponse updateInfSeller(String sellerId,String shopName, MultipartFile avatar) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(()-> new AppException(ErrorCode.SELLER_NOT_FOUND));
        boolean shouldUpdate = false;

        // Kiểm tra và cập nhật avatar nếu có thay đổi
        if (avatar != null && !avatar.isEmpty()) {
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
}