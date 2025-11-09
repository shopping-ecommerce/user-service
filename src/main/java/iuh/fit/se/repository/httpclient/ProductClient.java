package iuh.fit.se.repository.httpclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.ProductResponse;
import iuh.fit.se.dto.response.enums.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@FeignClient(
        name = "product-service",
        configuration = { ProductClient.Configuration.class }
)
public interface ProductClient {

    @GetMapping("/searchByProduct/{productId}")
    ApiResponse<ProductResponse> searchById(@PathVariable("productId") String productId);

    @PostMapping("/deleteProducts")
    ApiResponse<ProductResponse> deleteProducts(
            @RequestParam("sellerId") String sellerId,
            @RequestParam("reason") String reason
    );

    @PostMapping("/suspendAllBySeller")
    ApiResponse<Void> suspendAllProductsBySeller(
            @RequestParam("sellerId") String sellerId,
            @RequestParam("reason") String reason
    );

    @PostMapping("/activateAllBySeller")
    ApiResponse<Void> activateAllProductsBySeller(
            @RequestParam("sellerId") String sellerId
    );

    @PostMapping("/approve/{productId}")
    public ApiResponse<ProductResponse> approveProduct(
            @PathVariable("productId") String productId,
            @RequestParam("status") Status status,
            @RequestParam(value = "reason", required = false) String reason
    );

    class Configuration {

        @Bean
        public JacksonEncoder jacksonEncoder() {
            return new JacksonEncoder(objectMapper());
        }

        @Bean
        public JacksonDecoder jacksonDecoder() {
            return new JacksonDecoder(objectMapper());
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper;
        }

        /**
         * Interceptor null-safe cho cả HTTP thread lẫn @Scheduled job.
         * Ưu tiên:
         * 1) Header Authorization từ request hiện hành (nếu có)
         * 2) Token từ SecurityContext (JwtAuthenticationToken), nếu có
         * 3) Fallback: INTERNAL_SERVICE_TOKEN (cấu hình ở application.yml)
         */
        @Bean
        public RequestInterceptor authInterceptor(
                @Value("${feign.auth.internal-token:}") String internalToken
        ) {
            return template -> {
                String token = null;

                // (1) Lấy từ request hiện hành
                var attrs = RequestContextHolder.getRequestAttributes();
                if (attrs instanceof ServletRequestAttributes sra) {
                    token = sra.getRequest().getHeader("Authorization");
                }

                // (2) Lấy từ SecurityContext nếu chưa có
                if (!StringUtils.hasText(token)) {
                    var context = SecurityContextHolder.getContext();
                    if (context != null && context.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        token = "Bearer " + jwtAuth.getToken().getTokenValue();
                    }
                }

                // (3) Fallback: token nội bộ
                if (!StringUtils.hasText(token) && StringUtils.hasText(internalToken)) {
                    token = internalToken.startsWith("Bearer ") ? internalToken : "Bearer " + internalToken;
                }

                if (StringUtils.hasText(token)) {
                    template.header("Authorization", token);
                }
            };
        }
    }
}
