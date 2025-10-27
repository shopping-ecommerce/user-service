package iuh.fit.se.dto.response.records;

import lombok.Builder;

@Builder
public record OptionMediaGroup(
        String optionName,     // ví dụ "Color"
        String optionValue,    // "Black"
        String image
) {}
