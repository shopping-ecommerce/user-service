package iuh.fit.se.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.se.entity.Address;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressUpdateRequest {
    @JsonProperty("user_id")
    String userId;
    String oldAddress;
    Address newAddress;
}
