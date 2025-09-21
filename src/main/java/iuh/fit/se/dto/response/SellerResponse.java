package iuh.fit.se.dto.response;

import iuh.fit.se.entity.enums.SellerStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerResponse {
    String id;
    String userId;
    String shopName;
    String avatarLink;
    String email;
    List<String> identificationLink;
    double wallet;
    LocalDateTime registrationDate;
    String address;
    SellerStatusEnum status;
}