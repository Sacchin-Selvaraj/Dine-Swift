package com.dineswift.Api_Auth.Service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class GatewayRouteConfig {

    @Value("${dineswift.user-service-url}")
    private String userServiceUrl;

    @Value("${dineswift.restaurant-url}")
    private String restaurantServiceUrl;


    @Bean
    public RouteLocator createCustomRouteMatcher(RouteLocatorBuilder builder){
        log.info("Configuring Gateway Routes");
        return builder.routes()
                .route("path_route_user_service", r -> r.path("/user/**")
                        .uri(userServiceUrl))
                .route("path_route_restaurant_service", r -> r.path("/restaurant/**")
                        .uri(restaurantServiceUrl))
                .build();
    }
}
