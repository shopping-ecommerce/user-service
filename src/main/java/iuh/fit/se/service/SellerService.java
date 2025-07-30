package iuh.fit.se.service;

import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.SellerResponse;

import java.util.List;

public interface SellerService {
    SellerResponse createSeller(SellerRegistrationRequest request);
    SellerResponse verifySeller(SellerVerifyRequest request);

    SellerResponse searchByUserId(String userId);

    List<SellerResponse> searchSellerPending();
}
