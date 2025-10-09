package com.dineswift.userservice.config;

import com.dineswift.userservice.exception.RemoteApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${dineswift.restaurant-service.orderItem-service.url}")
    private String orderItemServiceUrl;

    @Bean
    public RestClient.Builder genericRestClientBuilder(){
        return RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .requestInterceptor((request, body, execution) ->{

                    log.info("Making request to URL: {}", request.getURI());
                    return execution.execute(request,body);
                })
                .defaultStatusHandler(HttpStatusCode::isError,(request, response) -> {

                    log.error("Remote service call failed with status: {}", response.getStatusCode());

                    throw new RemoteApiException(
                            "Service call failed with status: " + response.getStatusCode()
                    );

                });

       }


       @Bean
       public RestClient orderItemServiceRestClient(RestClient.Builder genericRestClientBuilder){
           return genericRestClientBuilder
                   .baseUrl(orderItemServiceUrl)
                   .build();
       }
}

