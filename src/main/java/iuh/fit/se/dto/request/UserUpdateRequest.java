package iuh.fit.se.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// @JsonIgnoreProperties(value = {"email"})
public class UserUpdateRequest {
    @JsonProperty("id")
    String id;
    @JsonProperty("firstName")
    String firstName;
    @JsonProperty("lastName")
    String lastName;
    @JsonProperty("birthdate")
    String birthdate;
    @JsonProperty("phone")
    String phone;
}
