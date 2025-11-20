package iuh.fit.se.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
    String id;
    String email;
//    Set<RoleResponse> roles;
//    LocalDateTime createdTime;
//    LocalDateTime modifiedTime;
//    String publicId;
}