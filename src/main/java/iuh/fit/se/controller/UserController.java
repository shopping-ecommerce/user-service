package iuh.fit.se.controller;

import java.util.List;
import java.util.Map;

import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.ProductResponse;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.service.SellerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/profiles")
public class UserController {
    UserService userService;
    SellerService sellerService;

    /**
     * Get all users.
     *
     * @return a list of all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.findUsers())
                .build();
    }

    /**
     * Get a user by ID.
     *
     * @param id the unique ID of the user
     * @return the user information
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.findById(id))
                .build();
    }

    /**
     * Search users by a query string.
     *
     * @param query the search query to filter users
     * @return a list of users matching the search query
     */
    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam(name = "q") String query) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.searchUsers(query))
                .build();
    }

    /**
     * Create a new user.
     *
     * @param request the user creation request containing user details
     * @return the created user
     */
    @PostMapping("/create")
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request)
            throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("UserClientRequest JSON: {}", objectMapper.writeValueAsString(request));
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    /**
     * Update an existing user.
     *
     * @param request the user update request containing updated user details
     * @return the updated user
     */
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PostMapping("/updateProfile")
    public ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(request))
                .build();
    }

    /**
     * Delete a user by ID.
     *
     * @param id the unique ID of the user to delete
     * @return a confirmation message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    /**
     * Delete multiple users.
     *
     * @param requestBody a map containing a list of user IDs to delete
     * @return a confirmation message
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ApiResponse<String> deleteUsers(@RequestBody Map<String, List<String>> requestBody) {
        List<String> ids = requestBody.get("ids");
        userService.deleteUsers(ids);
        return ApiResponse.<String>builder().result("Users have been deleted").build();
    }

    @GetMapping("/getAccount/{id}")
    public ApiResponse<UserResponse> getAccount(@PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.findByAccount(id))
                .build();
    }

    @PostMapping("/updateAvatar")
    public ApiResponse<UserResponse> uploadAvatar(@RequestParam("files") MultipartFile files,
                                                              @RequestParam("id") String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateAvatar(id,files))
                .build();
    }

    @GetMapping("/getMyProfile")
    public ApiResponse<UserResponse> myInfo(){
    return ApiResponse.<UserResponse>builder()
            .result(userService.getMyInfo())
            .build();
    }


    /**
     * Add a new address for a user.
     *
     * @param request the address add request containing user ID and address details
     * @return the updated user information
     */
    @PostMapping("/address/add")
    public ApiResponse<UserResponse> addAddress(@RequestBody AddressAddRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.addAddress(request))
                .build();
    }

    /**
     * Update an existing address for a user.
     *
     * @param request the address update request containing user ID, old address, and new address details
     * @return the updated user information
     */
    @PostMapping("/address/update")
    public ApiResponse<UserResponse> updateAddress(@RequestBody AddressUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateAddress(request))
                .build();
    }

    /**
     * Delete an address for a user.
     *
     * @param request the address delete request containing user ID and address to delete
     * @return the updated user information
     */
    @PostMapping("/address/delete")
    public ApiResponse<UserResponse> removeAddress(@RequestBody AddressDeleteRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.removeAddress(request))
                .build();
    }

    /**
     * Set an address as default for a user.
     *
     * @param request the address default request containing user ID and address to set as default
     * @return the updated user information
     */
    @PostMapping("/address/default")
    public ApiResponse<UserResponse> setDefaultAddress(@RequestBody AddressDefaultRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.setDefaultAddress(request))
                .build();
    }


    @PostMapping("/favorite/add")
    public ApiResponse<UserResponse> addFavorite(@RequestBody FavoriteRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.addFavoriteProduct(request))
                .build();
    }

    /**
     * Remove a product from user's favorites.
     *
     * @param request the favorite request containing user ID and product ID
     * @return the updated user information
     */
    @PostMapping("/favorite/remove")
    public ApiResponse<UserResponse> removeFavorite(@RequestBody FavoriteRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.removeFavoriteProduct(request))
                .build();
    }

    /**
     * Get all favorite products for a user.
     *
     * @param userId the user ID to get favorite products for
     * @return a list of favorite products
     */
    @GetMapping("/favorite/{userId}")
    public ApiResponse<List<ProductResponse>> getFavoriteProducts(@PathVariable String userId) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(userService.getFavoriteProducts(userId))
                .build();
    }
}