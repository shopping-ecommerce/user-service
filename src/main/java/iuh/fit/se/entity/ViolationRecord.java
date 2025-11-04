package iuh.fit.se.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "violation_records")

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ViolationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
            @JsonIgnore
    Seller seller;

    @Column(name = "violation_type")
    String violationType;

    @Column(name = "description", length = 1000)
    String description;

    @ElementCollection
    @CollectionTable(name = "violation_evidences", joinColumns = @JoinColumn(name = "violation_id"))
    @Column(name = "evidence_url", length = 512)
    @Builder.Default
    List<String> evidenceUrls = new ArrayList<>();

    @Column(name = "reported_at")
    LocalDateTime reportedAt;

    @Column(name = "violation_number")
    Integer violationNumber;
}
