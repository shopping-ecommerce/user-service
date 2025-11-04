package iuh.fit.se.service;

import iuh.fit.se.dto.request.ReportViolationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.ViolationRecord;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SellerService {
    SellerResponse createSeller(MultipartFile avatar, List<MultipartFile> identifications,String userId,String shopName,String email,String address);
    SellerResponse verifySeller(SellerVerifyRequest request);

    SellerResponse searchByUserId(String userId);
    SellerResponse searchBySellerId(String sellerId);

    SellerResponse updateInfSeller(String sellerId,String shopName,MultipartFile avatar,String address,String email);
    List<SellerResponse> searchSellerPending();

    List<SellerResponse> searchSellerApproved();

    List<SellerResponse> getAllSellers();

    void deleteSellers(List<String> sellerIds);

    @Transactional
    void deleteSeller(String sellerId, String reason);

    List<String> getAllSellerEmails();
    SellerResponse reportViolation(ReportViolationRequest request);
    SellerResponse unsuspendSeller(String sellerId);
    void autoUnsuspendExpiredSellers();
    List<SellerResponse> getAllSellerStatusSuspended();
    List<ViolationRecord> getViolationHistory(String sellerId);

    @Transactional
    int suspendAllApproved();
}
