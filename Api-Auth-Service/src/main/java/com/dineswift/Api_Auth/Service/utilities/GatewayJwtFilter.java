package com.dineswift.Api_Auth.Service.utilities;

import com.dineswift.Api_Auth.Service.exception.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayJwtFilter implements WebFilter {

    private final JwtUtilities jwtUtilities;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }
        log.info("Validating JWT token for protected endpoint: {}", path);
        String authToken;
        authToken = extractToken(exchange);
        try {
            if (authToken == null || !validateJwtToken(authToken,exchange)) {
                log.error("Invalid JWT token");
                return onAuthenticationFailure(exchange,"Invalid JWT token");
            }
        } catch (TokenException e) {
            log.error("JWT Token is expired");
            return onAuthenticationFailure(exchange,"Token is expired Please log in again");
        }
        log.info("JWT token is valid passing request to the next filter");
        Map<String,Object> claims = parseClaims(authToken);
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Auth-User",claims.get("authId").toString())
                .header("X-Roles", getRolesAsString(claims))
                .header("Authorization", "Bearer " + authToken)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(modifiedRequest).build();

        log.info("Setting the Authentication Object in the Security Context");
        Authentication authentication = generateAuthenticationFromToken(authToken);

        return chain.filter(mutatedExchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private String getRolesAsString(Map<String, Object> claims) {
        try {
            List<String> roles = (List<String>) claims.get("roles");
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

    private boolean validateJwtToken(String authToken,ServerWebExchange exchange) {
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
        List<String> publicEndpoints = List.of(
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
        );
         return publicEndpoints.stream()
                 .anyMatch(pattern -> pathMatcher.match(pattern,path));
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
