package iuh.fit.se.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerRegistrationRequest {
    @JsonProperty("userId")
    String userId;
    @JsonProperty("shopName")
    @NotEmpty(message = "Shop name cannot be empty")
    @NotBlank(message = "Shop name cannot be blank")
    @Pattern(regexp = "^[\\p{L}0-9\\s]{3,50}$",
            message = "Shop name must be between 3 and 50 characters and can only contain letters, numbers, and spaces")
    String shopName;

    @JsonProperty("avatarLink")
    String avatarLink;

    @JsonProperty("identificationLinks")
    List<String> identificationLinks;
}