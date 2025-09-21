package iuh.fit.se.dto.response.records;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Size(
        String size,
        BigDecimal  price,
        BigDecimal  compareAtPrice,
        Integer quantity,
        Boolean available
) {
}
