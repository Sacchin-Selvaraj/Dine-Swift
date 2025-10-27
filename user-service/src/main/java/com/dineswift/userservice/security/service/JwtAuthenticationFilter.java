package com.dineswift.userservice.security.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtilities jwtUtilities;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Set<String> whitelisted = Set.of("/user/login", "/user/sign-up");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return whitelisted.stream().anyMatch(p -> pathMatcher.match(p, path));
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("Processing JWT authentication for request: {}", request.getRequestURI());
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No Authorization header found or it is invalid");
            sendErrorResponse(response,"Missing or invalid Authorization header");
            return;
        }
        log.info("Extracting JWT token from Authorization header");
        String token = authHeader.substring(7);
        
        if (jwtUtilities.validateJwtToken(token)) {
            log.info("JWT token is valid, extracting claims");
            Claims claims = jwtUtilities.extractClaims(token);
            String username = claims.getSubject();

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, null, getAuthorities(claims));

            authToken.setDetails(new HashMap<String, Object>(claims));
            log.info("Setting authentication in SecurityContext");
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }else {
            sendErrorResponse(response,"Invalid or expired JWT token");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String responseBody = String.format(
                "{\"timestamp\": \"%s\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                Instant.now(), message
        );

        response.getWriter().write(responseBody);
    }
}