package iuh.fit.se.dto.response.records;

import lombok.Builder;

@Builder
public record Image(String url,Integer position) {
}
