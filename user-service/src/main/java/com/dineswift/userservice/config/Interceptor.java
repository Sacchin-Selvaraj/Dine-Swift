package com.dineswift.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@Slf4j
@Configuration
public class Interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Incoming request data log: uri={}, method={}", request.getRequestURI(), request.getMethod());
        log.info("Headers: {}", request.getHeaderNames());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Bean
    public WebFilter loggingFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("USER SERVICE RECEIVED: URI={}, Method={}", request.getURI(), request.getMethod());
            log.info("USER SERVICE RECEIVED: Headers={}", request.getHeaders().entrySet());

            // Crucial: check for your Authorization/custom headers here!
            // This log will execute even before the main security filter chain.
            // Look for the "Authorization" header or the "X-Auth-User-ID" header.

            return chain.filter(exchange);
        };
    }
}

