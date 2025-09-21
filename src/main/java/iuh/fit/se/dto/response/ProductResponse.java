package iuh.fit.se.dto.response;

import iuh.fit.se.dto.response.enums.Status;
import iuh.fit.se.dto.response.records.Image;
import iuh.fit.se.dto.response.records.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String sellerId;
    String name;
    String description;
    List<Image> images;
    List<Size> sizes;
    Status status;
    String categoryId;
    Instant createdAt;
}
