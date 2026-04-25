package ps.emall.orderhub.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Feign Client configuration for cross-microservice calls.
 * Configures timeouts, retries, logging, and error decoding.
 */
@Configuration
public class FeignConfig {

    /**
     * Connection timeout: 5 seconds
     * Read timeout: 10 seconds
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,   // connectTimeout
                10, TimeUnit.SECONDS,  // readTimeout
                true                    // followRedirects
        );
    }

    /**
     * Retry configuration:
     * - period: 1 second between retries
     * - maxPeriod: 3 seconds max between retries
     * - maxAttempts: 3 total attempts
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

    /**
     * Log level for Feign client (set to BASIC for production, FULL for debugging).
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Default error decoder - Feign will throw FeignException with HTTP status.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

    // Add this bean to the existing FeignConfig class:

    @Bean
    public RequestInterceptor internalAuthRequestInterceptor(
            @Value("${internal.auth.username}") String username,
            @Value("${internal.auth.password}") String password) {
        return requestTemplate -> {
            String credentials = username + ":" + password;
            String encoded = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            requestTemplate.header("Authorization", "Basic " + encoded);
        };
    }
}

