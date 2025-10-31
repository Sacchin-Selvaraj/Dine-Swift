package com.dineswift.userservice.config;

import com.dineswift.userservice.exception.ErrorResponse;
import com.dineswift.userservice.exception.RemoteApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                    Map<String,String> headers = getDefaultHeaders();
                    request.getHeaders().add("X-Auth-User",headers.get("X-Auth-User"));
                    request.getHeaders().add("X-Roles",headers.get("X-Roles"));
                    log.info("Making request to URL: {}", request.getURI());
                    log.info("Request Headers: {}", request.getHeaders());
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

    private Map<String, String> getDefaultHeaders() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes==null)
            log.info("No request attributes found");
        assert attributes != null;
        log.info("Extracting headers from the initial request");
        HttpServletRequest initialRequest = attributes.getRequest();
        Map<String,String> headers = new HashMap<>();
        headers.put("X-Auth-User", initialRequest.getHeader("X-Auth-User"));
        headers.put("X-Roles", initialRequest.getHeader("X-Roles"));
        return headers;
    }


    @Bean
       public RestClient restClient(RestClient.Builder genericRestClientBuilder){
           return genericRestClientBuilder
                   .baseUrl(restaurantServiceUrl)
                   .build();
       }
}

