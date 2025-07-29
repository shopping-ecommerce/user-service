package iuh.fit.se.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iuh.fit.se.dto.request.UserCreationRequest;
import iuh.fit.se.dto.request.UserUpdateRequest;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.UserResponse;
import iuh.fit.se.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/profiles")
public class UserController {
    UserService userService;

    /**
     * Get all users.
     *
     * @return a list of all users
     */
    @PreAuthorize("hasRole('MANAGER')")
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
     * @param id      the unique ID of the user to update
     * @param request the user update request containing updated user details
     * @return the updated user
     */
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(id, request))
                .build();
    }

    /**
     * Delete a user by ID.
     *
     * @param id the unique ID of the user to delete
     * @return a confirmation message
     */
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
}
