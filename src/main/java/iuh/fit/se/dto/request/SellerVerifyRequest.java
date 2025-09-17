package iuh.fit.se.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SellerVerifyRequest {
    @JsonProperty("sellerId")
    String sellerId;
    @JsonProperty("status")
    String status;
    String reason;
}
