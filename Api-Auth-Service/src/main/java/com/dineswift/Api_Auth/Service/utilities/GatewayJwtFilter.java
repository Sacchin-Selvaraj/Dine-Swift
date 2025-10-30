package com.dineswift.Api_Auth.Service.utilities;


import com.dineswift.Api_Auth.Service.payload.RoleName;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (authToken == null || !validateJwtToken(authToken)) {
            log.error("Invalid or expired JWT token");
            return onAuthenticationFailure(exchange,"Invalid or expired JWT token");
        }
        log.info("JWT token is valid passing request to the next filter");
        Map<String,Object> claims = parseClaims(authToken);
        log.info("Extracted Claims: {}", claims);
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Auth-User",claims.get("authId").toString())
                .header("X-Roles", getRolesAsString(claims))
                .header("Authorization", "Bearer " + authToken)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(modifiedRequest).build();
        log.info("Mutated Request Headers: {}", mutatedExchange.getRequest().getHeaders());

        log.info("Setting the Authentication Object in the Security Context");
        Authentication authentication = generateAuthenticationFromToken(authToken);

        return chain.filter(mutatedExchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private String getRolesAsString(Map<String, Object> claims) {
        try {
            List<String> roles = (List<String>) claims.get("roles");
            log.info("Extracted roles from claims: {}", roles);
            return String.join(",", roles);
        } catch (NullPointerException | ClassCastException e) {
            log.error("Error while extracting roles from claims: {}", e.getMessage());
            return "ROLE_UNDEFINED";
        }
    }

    private Map<String,Object> parseClaims(String authToken) {
        log.info("Parsing claims from JWT token in JwtUtilities");
        return jwtUtilities.extractClaims(authToken);
    }

    private Authentication generateAuthenticationFromToken(String authToken) {
        log.info("Generating Authentication object from JWT token");
        String username = jwtUtilities.extractUsername(authToken);
        return new UsernamePasswordAuthenticationToken(username,null,null);
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
                path.startsWith("/auth/login") ||
                path.startsWith("/auth/refresh-token");
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
