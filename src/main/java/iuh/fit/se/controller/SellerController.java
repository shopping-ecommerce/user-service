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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/sellers")
public class SellerController {
    SellerService sellerService;

    @PreAuthorize("hasAuthority('CREATE_SELLER')")
    @PostMapping("/registrationSeller")
    public ApiResponse<SellerResponse> registrationSeller(@RequestPart("avatar")MultipartFile avatar,
                                                          @RequestPart("identifications") List<MultipartFile> identifications,
                                                          @RequestParam("userId") String userId,
                                                          @RequestParam("shopName") String shopName,
                                                          @RequestParam("email") String email,
                                                          @RequestParam("address") String address) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.createSeller(avatar, identifications, userId, shopName,email,address))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/verifySeller")
    public ApiResponse<SellerResponse> verifySeller(@RequestBody SellerVerifyRequest request) {

        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.verifySeller(request))
                .build();
    }


    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasAuthority('UPDATE_SELLER')")
    @PostMapping("/updateInfSeller")
    public ApiResponse<SellerResponse> updateInfSeller(@RequestPart("avatar") MultipartFile avatar,
                                                       @RequestParam("sellerId") String sellerId,
                                                       @RequestParam("shopName") String shopName) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.updateInfSeller(sellerId,  shopName,avatar))
                .build();
    }

    @GetMapping("/searchByUserId/{userId}" )
    public ApiResponse<SellerResponse> searchByUserId(@PathVariable("userId") String userId) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.searchByUserId(userId))
                .build();
    }

    @GetMapping("/searchBySellerId/{sellerId}" )
    public ApiResponse<SellerResponse> searchBySellerId(@PathVariable("sellerId") String sellerId) {
        return ApiResponse.<SellerResponse>builder()
                .code(200)
                .result(sellerService.searchBySellerId(sellerId))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ApiResponse<List<SellerResponse>> getAllSellers() {
        return ApiResponse.<List<SellerResponse>>builder()
                .code(200)
                .result(sellerService.getAllSellers())
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteSellers")
    public ApiResponse<Void> deleteSellers(@RequestBody List<String> sellerIds) {
        sellerService.deleteSellers(sellerIds);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã cập nhật trạng thái REJECTED cho " + (sellerIds == null ? 0 : sellerIds.size()) + " seller")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_SELLER')")
    @DeleteMapping("/deleteSeller")
    public ApiResponse<Void> deleteSeller(@RequestParam("sellerId") String sellerId, @RequestParam("reason") String reason) {
        sellerService.deleteSeller(sellerId,reason);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã xóa Seller " + sellerId  + " seller")
                .build();
    }
}