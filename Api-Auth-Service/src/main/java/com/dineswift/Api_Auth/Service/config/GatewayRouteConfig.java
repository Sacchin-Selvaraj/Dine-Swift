package com.dineswift.Api_Auth.Service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class GatewayRouteConfig {

    @Bean
    public RouteLocator createCustomRouteMatcher(RouteLocatorBuilder builder){
        log.info("Configuring Gateway Routes");
        return builder.routes()
                .route("path_route_user_service", r -> r.path("/user/**")
                        .uri("lb://USER-SERVICE"))
                .route("user_service_booking", r -> r.path("/booking/**")
                        .uri("lb://USER-SERVICE"))
                .route("user_service_cart", r -> r.path("/cart/**")
                        .uri("lb://USER-SERVICE"))
                .route("user_service_verification", r -> r.path("/user-verification/**")
                        .uri("lb://USER-SERVICE"))
                .route("user_service_verification", r -> r.path("/user-password/**")
                        .uri("lb://USER-SERVICE"))
                .route("path_route_restaurant_service", r -> r.path("/restaurant/**")
                        .uri("lb://RESTAURANT-SERVICE"))
                .build();
    }
}
