package iuh.fit.se.repository.httpclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import iuh.fit.se.config.AuthenticationRequestInterceptor;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service",configuration = {ProductClient.Configuration.class, AuthenticationRequestInterceptor.class})
public interface ProductClient {
    @GetMapping("/searchByProduct/{productId}")
    ApiResponse<ProductResponse> searchById(@PathVariable("productId") String productId);
    @PostMapping("/deleteProducts")
    public ApiResponse<ProductResponse> deleteProducts(
            @RequestParam("sellerId") String sellerId,
            @RequestParam("reason") String reason
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
    }
}
