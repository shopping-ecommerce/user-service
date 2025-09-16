package iuh.fit.se.service;

import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.SellerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SellerService {
    SellerResponse createSeller(MultipartFile avatar, List<MultipartFile> identifications,String userId,String shopName,String email,String address);
    SellerResponse verifySeller(SellerVerifyRequest request);

    SellerResponse searchByUserId(String userId);
    SellerResponse searchBySellerId(String sellerId);

    SellerResponse updateInfSeller(String sellerId,String shopName,MultipartFile avatar);
    List<SellerResponse> searchSellerPending();
}
