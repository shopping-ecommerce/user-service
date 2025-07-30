package iuh.fit.se.controller;

import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.request.SellerVerifyRequest;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.service.SellerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/sellers")
public class SellerController {
    SellerService sellerService;
    @PostMapping("/registrationSeller")
    public ApiResponse<SellerResponse> registrationSeller(@RequestBody SellerRegistrationRequest request) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.createSeller(request))
                .build();
    }

    @PostMapping("/verifySeller")
    public ApiResponse<SellerResponse> verifySeller(@RequestBody SellerVerifyRequest request) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.verifySeller(request))
                .build();
    }

    @GetMapping("/sellerPending")
    public ApiResponse<List<SellerResponse>> searchSellerPending() {
        List<SellerResponse> sellers = sellerService.searchSellerPending();
        if (sellers.isEmpty()) {
            return ApiResponse.<List<SellerResponse>>builder()
                    .code(200)
                    .message("Không có hồ sơ nào cần duyệt")  // Add message field to ApiResponse
                    .result(List.of())  // Empty list of SellerResponse
                    .build();
        }
        return ApiResponse.<List<SellerResponse>>builder()
                .code(200)
                .message("Sellers retrieved successfully")
                .result(sellers)
                .build();
    }
}