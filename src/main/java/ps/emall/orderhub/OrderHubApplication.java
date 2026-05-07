package ps.emall.orderhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class OrderHubApplication {

    private static final String APPLICATION_TIME_ZONE = "Asia/Gaza";

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone(APPLICATION_TIME_ZONE));
        SpringApplication.run(OrderHubApplication.class, args);
    }

}
