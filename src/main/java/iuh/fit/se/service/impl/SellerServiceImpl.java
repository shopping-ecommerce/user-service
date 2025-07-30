package iuh.fit.se.service.impl;

import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.Seller;
import iuh.fit.se.entity.User;
import iuh.fit.se.enums.SellerStatusEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.SellerMapper;
import iuh.fit.se.repository.SellerRepository;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    UserRepository userRepository;
    SellerRepository sellerRepository;
    SellerMapper sellerMapper;

    @Override
    public SellerResponse createSeller(SellerRegistrationRequest request) {
        // 1. Kiểm tra User tồn tại
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem User đã đăng ký làm Seller chưa
        Optional<Seller> existingSeller = sellerRepository.findByUserId(user.getId());
        Seller seller;

        if (existingSeller.isPresent()) {
            seller = existingSeller.get();
            // Trường hợp REJECTED: Cập nhật bản ghi hiện có
            seller.setShopName(request.getShopName());
            seller.setAvatarLink(request.getAvatarLink());
            seller.setIdentificationLinks(request.getIdentificationLinks());
            seller.setStatus(SellerStatusEnum.PENDING);
            seller.setRegistrationDate(LocalDateTime.now());
            seller.setModifiedTime(LocalDateTime.now());
        } else {
            // Tạo Seller mới nếu chưa có
            seller = Seller.builder()
                    .user(user)
                    .shopName(request.getShopName())
                    .avatarLink(request.getAvatarLink())
                    .identificationLinks(request.getIdentificationLinks())
                    .build();
        }
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse verifySeller(SellerVerifyRequest request) {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        seller.setStatus(request.getStatus().toUpperCase().equalsIgnoreCase(SellerStatusEnum.APPROVED.toString()) ? SellerStatusEnum.APPROVED : SellerStatusEnum.REJECTED);
        return sellerMapper.toSellerResponse(sellerRepository.save(seller));
    }

    @Override
    public SellerResponse searchByUserId(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
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