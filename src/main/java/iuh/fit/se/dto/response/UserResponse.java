package iuh.fit.se.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import iuh.fit.se.entity.Address;
import iuh.fit.se.enums.UserStatusEnum;
import iuh.fit.se.enums.UserTierEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String accountId;
    String firstName;
    String lastName;
    int points;
    UserTierEnum tier;
    List<Address> addresses = new ArrayList<>();
    UserStatusEnum status;
    LocalDateTime createdTime;
    LocalDateTime modifiedTime;
    String birthdate;
    String publicId;
}
