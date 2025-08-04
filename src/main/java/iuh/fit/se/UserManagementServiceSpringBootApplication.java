package iuh.fit.se;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class UserManagementServiceSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceSpringBootApplication.class, args);
    }
}
