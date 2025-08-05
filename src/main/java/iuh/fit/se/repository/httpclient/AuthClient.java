package iuh.fit.se.repository.httpclient;

import iuh.fit.se.config.AuthenticationRequestInterceptor;
import iuh.fit.se.config.FeignConfiguration;
import iuh.fit.se.dto.request.AssignRoleRequest;
import iuh.fit.se.dto.response.AuthClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", configuration = {AuthenticationRequestInterceptor.class, FeignConfiguration.class})
public interface AuthClient {
    @PostMapping(value = "/assign-role" , consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    AuthClientResponse assignRoleToUser(@RequestBody AssignRoleRequest request);
}
