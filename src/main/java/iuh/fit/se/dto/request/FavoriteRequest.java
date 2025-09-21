package iuh.fit.se.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {
    String userId;
    String productId;
}
