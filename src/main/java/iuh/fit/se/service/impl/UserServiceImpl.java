package iuh.fit.se.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import feign.FeignException;
import iuh.fit.se.dto.request.*;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.FileClientResponse;
import iuh.fit.se.dto.response.ProductResponse;
import iuh.fit.se.dto.response.enums.Status;
import iuh.fit.se.entity.Address;
import iuh.fit.se.repository.httpclient.FileClient;
import iuh.fit.se.repository.httpclient.ProductClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.entity.User;
import iuh.fit.se.entity.enums.UserStatusEnum;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.mapper.UserMapper;
import iuh.fit.se.repository.UserRepository;
import iuh.fit.se.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileClient fileClient;
    private  final ProductClient productClient;
    /**
     * Creates a new user based on the provided UserCreationRequest.
     *
     * @param request the UserCreationRequest containing user details
     * @return UserResponse the created user details
     */
    @Transactional
    @Override
    public UserResponse createUser(UserCreationRequest request) {
        log.info("1111111111: " + request.getFirstName() + " " + request.getLastName() + request.getAccountId()+ request.getPublicId());
        // Ánh xạ và lưu user
        request.setPublicId("https://shopping-iuh-application.s3.ap-southeast-1.amazonaws.com/DefaultAvatar.jpg");
        User user = userMapper.toUser(request);
        user.setAccountId(request.getAccountId());
        //        user.setFirstName(request.getFirstName());
        //        user.setLastName(request.getLastName());
        //        User user = new User();
        //        user.setAccountId(request.getAccountId());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    /**
     * Updates an existing user identified by the given id.
     *
     * @param request the UserUpdateRequest containing updated user details
     * @return UserResponse the updated user details
     */
    @Override
    public UserResponse updateUser(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        user.setModifiedTime(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    /**
     * Deletes a user identified by the given id by marking it as deleted.
     *
     * @param id the id of the user to be deleted
     */
    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatusEnum.DELETED);
        userRepository.save(user);
    }
    //
    //    /**
    //     * Finds a user by their email address.
    //     *
    //     * @param email the email address of the user to be found
    //     * @return UserResponse the found user details
    //     */
    //    @Override
    //    public UserResponse findByEmail(String email) {
    //        User user = userRepository.findByEmail(email).orElseThrow(() -> new
    // AppException(ErrorCode.USER_NOT_FOUND));
    //        return objectMapper.convertValue(user, UserResponse.class);
    //    }

    /**
     * Retrieves all users.
     *
     * @return List<UserResponse> a list of all user details
     */
    @Override
    public List<UserResponse> findUsers() {
        List<User> users = userRepository.findAll();
        return objectMapper.convertValue(users, new TypeReference<List<UserResponse>>() {});
    }

    /**
     * Finds users by their role.
     *
     * @param role the role of the users to be found
     * @return List<UserResponse> a list of users with the specified role
     */
    //    @Override
    //    public List<UserResponse> findByRole(String role) {
    //        UserRoleEnum roleEnum = UserRoleEnum.valueOf(role.toUpperCase());
    //        List<User> users = userRepository.findByRole(roleEnum);
    //        return objectMapper.convertValue(users, new TypeReference<List<UserResponse>>() {});
    //    }

    /**
     * Finds a user by their id.
     *
     * @param id the id of the user to be found
     * @return UserResponse the found user details
     */
    @Override
    public UserResponse findById(String id) {
        Optional<User> user = userRepository.findById(id);
        return userMapper.toUserResponse(user.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserResponse findByAccount(String accountId) {
        User user = userRepository.findByAccountId(accountId);
        if (user == null) {
            throw new RuntimeException("User not found with accountId: " + accountId);
        }
        return objectMapper.convertValue(user, UserResponse.class);
    }

    /**
     * Deletes multiple users identified by their ids by marking them as deleted.
     *
     * @param ids a list of user ids to be deleted
     */
    @Override
    public void deleteUsers(List<String> ids) {
        ids.forEach(id -> {
            User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            user.setStatus(UserStatusEnum.DELETED);
            userRepository.save(user);
        });
    }

    /**
     * Searches for users based on a search query.
     *
     * @param searchQuery the query string to search for users
     * @return List<UserResponse> a list of users matching the search criteria
     */
    @Override
    public List<UserResponse> searchUsers(String searchQuery) {
        List<User> users = userRepository.findAll();
        String searchLower = searchQuery.toLowerCase();
        List<User> filteredUsers = users.stream()
                .filter(user -> user.getId().contains(searchQuery)
                        || user.getFirstName().toLowerCase().contains(searchLower)
                        || user.getLastName().toLowerCase().contains(searchLower)
//                        || user.getAddress().toLowerCase().contains(searchLower)
                        || (user.getFirstName().toLowerCase() + " "
                                        + user.getLastName().toLowerCase())
                                .contains(searchLower)
                        || (user.getLastName().toLowerCase() + " "
                                        + user.getFirstName().toLowerCase())
                                .contains(searchLower))
                .collect(Collectors.toList());
        return objectMapper.convertValue(filteredUsers, new TypeReference<List<UserResponse>>() {});
    }

    @Override
    public UserResponse updateAvatar(String userId, MultipartFile file) {
        log.info("Bắt đầu tải lên avatar cho userId: {}, tên tệp: {}", userId, file.getOriginalFilename());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (file == null || file.isEmpty()) {
            log.error("Tệp avatar rỗng hoặc null cho userId: {}", userId);
        }
        try {
            log.info("Gửi yêu cầu tải lên tệp {} đến File Service", file.getOriginalFilename());
            FileClientResponse fileClientResponse = fileClient.uploadFile(List.of(file));
            log.info("Phản hồi từ File Service: {}", fileClientResponse.getMessage());
            try {
                fileClient.deleteByUrl(DeleteRequest.builder().urls(List.of(user.getPublicId())).build());
            } catch (FeignException e) {
                log.error("Delete identifications failed: status={}, body={}", e.status(), e.contentUTF8());
                throw new AppException(ErrorCode.FILE_DELETE_FAILED);
            }
            user.setPublicId(fileClientResponse.getResult().get(0));
            return userMapper.toUserResponse(userRepository.save(user));
        } catch (FeignException e) {
            log.error("Lỗi khi gọi File Service cho userId {}: {}", userId, e.getMessage(), e);
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    @Override
    public UserResponse getMyInfo() {
        String accountId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByAccountId(accountId);
        if(user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return userMapper.toUserResponse(user);
    }


    @Transactional
    @Override
    public UserResponse addAddress(AddressAddRequest request) {
        log.info("Adding address {} for user {}", request.getAddress().getAddress(), request.getUserId());
        validateAddress(request.getAddress());
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getAddresses().stream().anyMatch(addr -> addr.getAddress().equals(request.getAddress().getAddress()))) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        if (request.getAddress().isDefault()) {
            user.getAddresses().forEach(addr -> addr.setDefault(false));
        }

        user.getAddresses().add(request.getAddress());
        ensureSingleDefaultAddress(user.getAddresses());
        user.getAddresses().size(); // Đánh dấu danh sách là "dirty"
        user.setModifiedTime(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse updateAddress(AddressUpdateRequest request) {
        log.info("Updating address from {} to {} for user {}", request.getOldAddress(), request.getNewAddress().getAddress(), request.getUserId());
        validateAddress(request.getNewAddress());
        if (request.getOldAddress() == null || request.getOldAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address addressToUpdate = null;
        for (Address addr : user.getAddresses()) {
            if (addr.getAddress().equals(request.getOldAddress())) {
                addressToUpdate = addr;
                break;
            }
        }

        if (addressToUpdate == null) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        if (!request.getOldAddress().equals(request.getNewAddress().getAddress()) &&
                user.getAddresses().stream().anyMatch(addr -> addr.getAddress().equals(request.getNewAddress().getAddress()))) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        if (request.getNewAddress().isDefault()) {
            user.getAddresses().forEach(addr -> addr.setDefault(false));
        }

        addressToUpdate.setAddress(request.getNewAddress().getAddress());
        addressToUpdate.setDefault(request.getNewAddress().isDefault());
        ensureSingleDefaultAddress(user.getAddresses());
        user.getAddresses().size(); // Đánh dấu danh sách là "dirty"
        user.setModifiedTime(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse removeAddress(AddressDeleteRequest request) {
        log.info("Removing address {} for user {}", request.getAddress(), request.getUserId());

        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean wasDefault = false;
        Address addressToRemove = null;
        for (Address addr : user.getAddresses()) {
            if (addr.getAddress().equals(request.getAddress())) {
                addressToRemove = addr;
                wasDefault = addr.isDefault();
                break;
            }
        }

        if (addressToRemove == null) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        user.getAddresses().remove(addressToRemove);

        if (wasDefault && !user.getAddresses().isEmpty()) {
            user.getAddresses().get(0).setDefault(true);
        }

        ensureSingleDefaultAddress(user.getAddresses());
        user.getAddresses().size(); // Đánh dấu danh sách là "dirty"
        user.setModifiedTime(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse setDefaultAddress(AddressDefaultRequest request) {
        log.info("Setting default address {} for user {}", request.getAddress(), request.getUserId());
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address addressToSetDefault = null;
        for (Address addr : user.getAddresses()) {
            if (addr.getAddress().equals(request.getAddress())) {
                addressToSetDefault = addr;
                break;
            }
        }

        if (addressToSetDefault == null) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }

        user.getAddresses().forEach(addr -> addr.setDefault(false));
        addressToSetDefault.setDefault(true);

        ensureSingleDefaultAddress(user.getAddresses());
        user.getAddresses().size(); // Đánh dấu danh sách là "dirty"
        user.setModifiedTime(LocalDateTime.now());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse addFavoriteProduct(FavoriteRequest request) {
        log.info("Adding favorite product {} for user {}", request.getProductId(), request.getUserId());

        // Validation
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Tìm user theo userId
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        try {
            // Kiểm tra sản phẩm có tồn tại và hợp lệ không
            ApiResponse<ProductResponse> productResponse = productClient.searchById(request.getProductId());

            if (productResponse == null || productResponse.getResult() == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            ProductResponse product = productResponse.getResult();

            // Kiểm tra trạng thái sản phẩm
            if (product.getStatus() != Status.AVAILABLE) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ACTIVE);
            }

            // Initialize favoriteProducts list if null
            if (user.getFavoriteProducts() == null) {
                user.setFavoriteProducts(new ArrayList<>());
            }

            // Kiểm tra sản phẩm đã tồn tại trong danh sách yêu thích chưa
            if (user.getFavoriteProducts().contains(request.getProductId())) {
                throw new AppException(ErrorCode.PRODUCT_ALREADY_IN_FAVORITES);
            }

            // Thêm sản phẩm vào danh sách yêu thích
            user.getFavoriteProducts().add(request.getProductId());
            user.setModifiedTime(LocalDateTime.now());

            // Lưu thay đổi
            User savedUser = userRepository.save(user);
            log.info("Successfully added product {} to favorites for user {}", request.getProductId(), request.getUserId());

            return userMapper.toUserResponse(savedUser);

        } catch (FeignException e) {
            log.error("Error calling Product Service for productId {}: {}", request.getProductId(), e.getMessage(), e);
            throw new AppException(ErrorCode.PRODUCT_SERVICE_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error when adding favorite product: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public UserResponse removeFavoriteProduct(FavoriteRequest request) {
        log.info("Removing favorite product {} for user {}", request.getProductId(), request.getUserId());

        // Validation
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (request.getProductId() == null || request.getProductId().trim().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Tìm user theo userId
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra favoriteProducts list
        if (user.getFavoriteProducts() == null || user.getFavoriteProducts().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_IN_FAVORITES);
        }

        // Kiểm tra sản phẩm có trong danh sách yêu thích không
        if (!user.getFavoriteProducts().contains(request.getProductId())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_IN_FAVORITES);
        }

        // Xóa sản phẩm khỏi danh sách yêu thích
        user.getFavoriteProducts().remove(request.getProductId());
        user.setModifiedTime(LocalDateTime.now());

        // Lưu thay đổi
        User savedUser = userRepository.save(user);
        log.info("Successfully removed product {} from favorites for user {}", request.getProductId(), request.getUserId());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public List<ProductResponse> getFavoriteProducts(String userId) {
        log.info("Getting favorite products for user {}", userId);

        // Validation
        if (userId == null || userId.trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Tìm user theo userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra danh sách sản phẩm yêu thích
        if (user.getFavoriteProducts() == null || user.getFavoriteProducts().isEmpty()) {
            log.info("User {} has no favorite products", userId);
            return new ArrayList<>();
        }

        List<ProductResponse> favoriteProducts = new ArrayList<>();
        List<String> invalidProductIds = new ArrayList<>();

        // Lấy thông tin chi tiết của từng sản phẩm yêu thích
        for (String productId : user.getFavoriteProducts()) {
            try {
                ApiResponse<ProductResponse> productResponse = productClient.searchById(productId);

                if (productResponse != null && productResponse.getResult() != null) {
                    ProductResponse product = productResponse.getResult();

                    // Chỉ thêm sản phẩm có trạng thái AVAILABLE
                    if (product.getStatus() == Status.AVAILABLE) {
                        favoriteProducts.add(product);
                    } else {
                        log.warn("Product {} is not available, status: {}", productId, product.getStatus());
                        invalidProductIds.add(productId);
                    }
                } else {
                    log.warn("Product {} not found", productId);
                    invalidProductIds.add(productId);
                }
            } catch (FeignException e) {
                log.error("Error calling Product Service for productId {}: {}", productId, e.getMessage());
                invalidProductIds.add(productId);
            } catch (Exception e) {
                log.error("Unexpected error when fetching product {}: {}", productId, e.getMessage());
                invalidProductIds.add(productId);
            }
        }

        // Xóa các sản phẩm không hợp lệ khỏi danh sách yêu thích
        if (!invalidProductIds.isEmpty()) {
            log.info("Removing {} invalid products from user {}'s favorites", invalidProductIds.size(), userId);
            user.getFavoriteProducts().removeAll(invalidProductIds);
            user.setModifiedTime(LocalDateTime.now());
            userRepository.save(user);
        }

        log.info("Retrieved {} favorite products for user {}", favoriteProducts.size(), userId);
        return favoriteProducts;
    }

    private void validateAddress(Address address) {
        if (address == null || address.getAddress() == null || address.getAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }
        if (address.getAddress().length() > 255) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }
    }

    private void validateAddressList(List<Address> addresses) {
        if (addresses != null && addresses.size() > 10) {
            throw new AppException(ErrorCode.INVALID_ADDRESS);
        }
        if (addresses != null && !addresses.isEmpty()) {
            long distinctAddresses = addresses.stream()
                    .map(Address::getAddress)
                    .distinct()
                    .count();
            if (distinctAddresses != addresses.size()) {
                throw new AppException(ErrorCode.INVALID_ADDRESS);
            }
        }
    }

    private void ensureSingleDefaultAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return;
        }

        boolean hasDefault = false;
        for (Address address : addresses) {
            if (address.isDefault()) {
                if (hasDefault) {
                    address.setDefault(false);
                } else {
                    hasDefault = true;
                }
            }
        }

        if (!hasDefault && !addresses.isEmpty()) {
            addresses.get(0).setDefault(true);
        }
    }
}
