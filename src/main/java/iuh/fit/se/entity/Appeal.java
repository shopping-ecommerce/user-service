package iuh.fit.se.entity;

import iuh.fit.se.entity.enums.AppealStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appeals")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    Seller seller;

    String violationRecordId; // Nếu khiếu nại về vi phạm cụ thể

    @Column(length = 2000)
    String reason; // Lý do khiếu nại

    @ElementCollection
    @CollectionTable(name = "appeal_evidences", joinColumns = @JoinColumn(name = "appeal_id"))
    @Column(name = "evidence_url", length = 512)
    @Builder.Default
    List<String> evidenceUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    AppealStatusEnum status = AppealStatusEnum.PENDING;

    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
//    String reviewedBy; // Admin ID

    @Column(length = 2000)
    String adminResponse; // Phản hồi từ admin

    @Column(name = "product_id")
    String productId; // Nếu khiếu nại về sản phẩm cụ thể
    @PrePersist
    void prePersist() {
        this.submittedAt = LocalDateTime.now();
        this.status = AppealStatusEnum.PENDING;
    }
}