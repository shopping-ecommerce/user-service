package iuh.fit.se.repository.httpclient;

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

@FeignClient(name = "fileService", url = "${file.service.url}", configuration = {FileClient.FormConfig.class, AuthenticationRequestInterceptor.class})
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
}