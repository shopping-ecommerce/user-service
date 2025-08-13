package iuh.fit.se.repository.httpclient;

import feign.Logger;
import feign.Retryer;
import feign.codec.Encoder;
import iuh.fit.se.config.AuthenticationRequestInterceptor;
import iuh.fit.se.dto.response.FileClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@FeignClient(name = "file-service", configuration = {FileClient.FormConfig.class, AuthenticationRequestInterceptor.class,FileClient.FeignRetryConfig.class,FileClient.FeignLogConfig.class})
public interface FileClient {
    @PostMapping(value = "/s3/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileClientResponse uploadFile(@RequestPart("files") List<MultipartFile> files);

    @Configuration
    class FormConfig {
        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder();
        }
    }

    @Configuration
    public class FeignRetryConfig{
        @Bean
        public Retryer feignRetryConfig(){
            return new Retryer.Default(100,1000,3);
        }
    }

    @Configuration
    class FeignLogConfig {
        @Bean
        Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }
    }
}