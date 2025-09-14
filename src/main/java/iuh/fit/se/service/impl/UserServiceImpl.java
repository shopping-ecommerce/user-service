package iuh.fit.se.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import feign.FeignException;
import iuh.fit.se.dto.response.FileClientResponse;
import iuh.fit.se.repository.httpclient.FileClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.entity.User;
import iuh.fit.se.enums.UserStatusEnum;
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
                        || user.getAddress().toLowerCase().contains(searchLower)
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

    //    @Override
    //    public UserResponse getMyInfo() {
    //        String email = SecurityContextHolder.getContext().getAuthentication().getName();
    //        User user = userRepository.findByEmail(email).orElseThrow(() -> new
    //                AppException(ErrorCode.USER_NOT_FOUND));
    //        return userMapper.toUserResponse(user);
    //    }
}
