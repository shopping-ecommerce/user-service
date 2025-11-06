package iuh.fit.se.dto.response;

import iuh.fit.se.entity.ViolationRecord;
import iuh.fit.se.entity.enums.SellerStatusEnum;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    LocalDateTime registrationDate;
    String address;
    SellerStatusEnum status;
    Integer violationCount;
    LocalDateTime suspendedAt;
    LocalDateTime suspensionEndDate;
    String suspensionReason;
    @Builder.Default
    List<ViolationRecord> violationHistory = new ArrayList<>();

}