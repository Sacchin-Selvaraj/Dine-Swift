package com.dineswift.Api_Auth.Service.utilities;

import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayJwtFilter implements WebFilter {

    private final JwtUtilities jwtUtilities;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("=== GATEWAY REQUEST ===");
        log.info("Path: {}", exchange.getRequest().getPath());
        log.info("Method: {}", exchange.getRequest().getMethod());
        log.info("Headers: {}", exchange.getRequest().getHeaders());
        log.info("URI: {}", exchange.getRequest().getURI());

        log.info("Get the path of the request: {}", exchange.getRequest().getURI().getPath());
        String path = exchange.getRequest().getURI().getPath();
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }
        log.info("Validating JWT token for protected endpoint: {}", path);
        String authToken = extractToken(exchange);
        if (authToken != null && !validateJwtToken(authToken)) {
            log.error("Invalid or expired JWT token");
            return onAuthenticationFailure(exchange,"Invalid or expired JWT token");
        }
        log.info("JWT token is valid passing request to the next filter");
        log.info("Auth Token: {}", authToken);
        ServerWebExchange mutatedExchange = exchange.mutate().request(builder -> builder.header("Authorization",authToken)).build();
        log.info("Header Authorization: {}", exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        return chain.filter(exchange);
    }

    private boolean validateJwtToken(String authToken) {
        log.info("Validating JWT token in JwtUtilities");
        return jwtUtilities.validateJwtToken(authToken);
    }

    private String extractToken(ServerWebExchange exchange) {
        log.info("Extracting JWT token from Authorization header");
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token!=null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
         return path.startsWith("/user/sign-up") ||
                path.startsWith("/user/login");
    }

    private Mono<Void> onAuthenticationFailure(ServerWebExchange exchange, String message) {
        log.error("Setting unauthorized response due to authentication failure");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseBody = String.format(
                "{\"timestamp\": \"%s\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                Instant.now(), message
        );

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(responseBody.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
