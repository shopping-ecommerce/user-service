package iuh.fit.se.service;

import java.util.List;

import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse findById(String id);

    UserResponse findByAccount(String account);

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateUser(UserUpdateRequest request);

    void deleteUser(String id);

    //    UserResponse findByEmail(String email);

    List<UserResponse> findUsers();

    //    List<UserResponse> findByRole(String role);

    void deleteUsers(List<String> ids);

    List<UserResponse> searchUsers(String searchQuery);

    UserResponse updateAvatar (String userId,MultipartFile file);

    UserResponse getMyInfo();
}
