package iuh.fit.se.entity;

import java.time.LocalDateTime;

import iuh.fit.se.entity.enums.SellerStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Sellers")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @Column(name = "shop_name")
    String shopName;

    @Column(name = "avatar_link")
    String avatarLink;

    @ElementCollection
    @CollectionTable(name = "seller_identification_links", joinColumns = @JoinColumn(name = "seller_id"))
    @Column(name = "identification_link")
    List<String> identificationLinks;

    String address;
    @Enumerated(EnumType.STRING)
    SellerStatusEnum status;

    String email;

    LocalDateTime registrationDate;

    LocalDateTime createdTime;

    LocalDateTime modifiedTime;
    @Column(name = "violation_count")
    Integer violationCount;
    @Column(name = "suspended_at")
    LocalDateTime suspendedAt;
    @Column(name = "suspension_end_date")
    LocalDateTime suspensionEndDate;
    @Column(name = "suspension_reason")
    String suspensionReason;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<ViolationRecord> violationHistory = new ArrayList<>();
    @PrePersist
    void prePersist() {
        this.registrationDate = LocalDateTime.now();
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
        this.status = SellerStatusEnum.PENDING;
        this.violationCount = 0;
    }

    @PreUpdate
    void preUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }
}