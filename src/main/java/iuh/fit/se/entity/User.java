package iuh.fit.se.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import iuh.fit.se.enums.UserStatusEnum;
import iuh.fit.se.enums.UserTierEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "account_id")
    String accountId;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    int points;

    @Enumerated(EnumType.STRING)
    UserTierEnum tier;

    String address;
    UserStatusEnum status;

    @Column(name = "created_time")
    LocalDateTime createdTime;

    @Column(name = "modified_time")
    LocalDateTime modifiedTime;

    @Column(name = "public_id")
    String publicId;

    @PrePersist
    void generateValue() {
        this.points = 0;
        this.tier = UserTierEnum.NONE;
        this.status = UserStatusEnum.AVAILABLE;
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
    }
}
