package com.dineswift.Api_Auth.Service.utilities;

import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
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
public class GatewayJwtFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
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
        ServerWebExchange mutatedExchange = exchange.mutate().build();
        return chain.filter(mutatedExchange);
    }

    private boolean validateJwtToken(String authToken) {

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
         return path.startsWith("/auth/") ||
                path.startsWith("/public/");
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
