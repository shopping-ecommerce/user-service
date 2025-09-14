package iuh.fit.se.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import iuh.fit.se.enums.UserStatusEnum;
import iuh.fit.se.enums.UserTierEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String id;

    @JsonProperty("accountId")
    String accountId;

    @NotEmpty(message = "First name must not be empty")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "NAME_INVALID")
    @JsonProperty("firstName")
    String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "NAME_INVALID")
    @JsonProperty("lastName")
    String lastName;

//    @NotEmpty(message = "Address must not be empty")
    String address;

    int points;
    UserTierEnum tier;
    UserStatusEnum status;
    String publicId;
    LocalDateTime createdTime;
    LocalDateTime modifiedTime;
}
