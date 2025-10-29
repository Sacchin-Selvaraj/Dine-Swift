package com.dineswift.Api_Auth.Service.config;

import com.dineswift.Api_Auth.Service.utilities.GatewayJwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class GatewaySecurityConfig {

    private final GatewayJwtFilter gatewayJwtFilter;

    @Bean
     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        log.info("Configuring SecurityWebFilterChain for Gateway");
        return httpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchangeSpec -> exchangeSpec
                        .pathMatchers("/user/sign-up","/user/login","/eureka/**","/favicon.ico")
                        .permitAll()
                        .anyExchange()
                        .authenticated())
                .addFilterBefore(gatewayJwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
     }
}
