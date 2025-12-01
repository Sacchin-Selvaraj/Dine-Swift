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
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchangeSpec -> exchangeSpec
                        .pathMatchers(
                                "/auth/**",
                                "/user/sign-up",
                                "/user/login",
                                "/user/password/**",
                                "/restaurant/employee/login",
                                "/restaurant/employee/sign-up",
                                "/restaurant/dish/search-dish/**",
                                "/restaurant/dish/search-dish-restaurant/**",
                                "/restaurant/menu/get-menu/**",
                                "/restaurant/menu/get-menu-details/**",
                                "/restaurant/get-restaurants/**",
                                "/restaurant/get-images/**",
                                "/restaurant/table/get-table/**",
                                "/restaurant/table/available-slots/**",
                                "/restaurant/table/available-slot/**",
                                "/restaurant/employee/forgot-password",
                                "/restaurant/employee/verify-forgot-password"

                        )
                        .permitAll()
                        .anyExchange()
                        .authenticated())
                .addFilterBefore(gatewayJwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
     }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
