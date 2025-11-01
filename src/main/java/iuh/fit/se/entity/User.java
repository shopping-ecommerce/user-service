package iuh.fit.se.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import iuh.fit.se.entity.enums.UserStatusEnum;
import iuh.fit.se.entity.enums.UserTierEnum;
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

    @ElementCollection
    @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    List<Address> addresses = new ArrayList<>(); // Sử dụng List<Address>

    @ElementCollection
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "product_id")
    List<String> favoriteProducts = new ArrayList<>(); // Danh sách product_id của sản phẩm yêu thích

    @Enumerated(EnumType.STRING)
    UserStatusEnum status;

    @Column(name = "created_time")
    LocalDateTime createdTime;

    @Column(name = "modified_time")
    LocalDateTime modifiedTime;

    @Column(name = "public_id")
    String publicId;

    String phone;
<<<<<<< Updated upstream

    String birthdate;
=======
>>>>>>> Stashed changes
    @PrePersist
    void generateValue() {
        this.points = 0;
        this.tier = UserTierEnum.NONE;
        this.status = UserStatusEnum.AVAILABLE;
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
        this.birthdate = "01-01-2000";
        this.addresses = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
    }

    @PreUpdate
    void updateModifiedTime() {
        this.modifiedTime = LocalDateTime.now();
    }
}
