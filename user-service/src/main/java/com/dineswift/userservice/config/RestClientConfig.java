package com.dineswift.userservice.config;

import com.dineswift.userservice.exception.ErrorResponse;
import com.dineswift.userservice.exception.RemoteApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${dineswift.restaurant-service.url}")
    private String restaurantServiceUrl;

    @Bean
    public RestClient.Builder genericRestClientBuilder(){
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .requestInterceptor((request, body, execution) ->{

                    log.info("Making request to URL: {}", request.getURI());
                    return execution.execute(request,body);
                })
                .defaultStatusHandler(HttpStatusCode::isError,(request, response) -> {

                    log.error("Remote service call failed with status: {}", response.getStatusCode());

                    String errorBody = "";
                    try {
                        errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        log.error("Failed to read error body from the Restaurant Service Response ", e);
                    }

                    throw new RemoteApiException(
                            "Service call failed with response : " + errorBody
                    );

                });
       }


       @Bean
       public RestClient restClient(RestClient.Builder genericRestClientBuilder){
           return genericRestClientBuilder
                   .baseUrl(restaurantServiceUrl)
                   .build();
       }
}

