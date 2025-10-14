package com.dineswift.restaurant_service.config;

import com.dineswift.restaurant_service.exception.RemoteApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${dineswift.user-service.url}")
    private String userServiceUrl;

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

                    throw new RemoteApiException(
                            "Service call failed with status: " + response.getStatusCode()
                    );

                });

       }


       @Bean
       public RestClient restClient(RestClient.Builder genericRestClientBuilder){
           return genericRestClientBuilder
                   .baseUrl(userServiceUrl)
                   .build();
       }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}

