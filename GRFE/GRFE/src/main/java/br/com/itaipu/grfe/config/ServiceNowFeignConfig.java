package br.com.itaipu.grfe.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ServiceNowFeignConfig {

    @Bean
    public RequestInterceptor serviceNowRequestInterceptor(
            @Value("${servicenow.username}") String username,
            @Value("${servicenow.password}") String password) {

        return requestTemplate -> {
            String auth = username + ":" + password;

            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            requestTemplate.header(
                    "Authorization",
                    "Basic " + encodedAuth
            );

            requestTemplate.header(
                    "Accept",
                    "application/json"
            );

            requestTemplate.header(
                    "Content-Type",
                    "application/json"
            );
        };
    }
}