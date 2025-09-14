package iuh.fit.se.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Address {
    @Column(name = "address")
    private String address;

    @Column(name = "is_default")
    @JsonProperty("is_default")
    private boolean isDefault;
}