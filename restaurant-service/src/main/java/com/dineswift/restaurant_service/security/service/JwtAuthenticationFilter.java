package com.dineswift.restaurant_service.security.service;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Set<String> whitelisted = Set.of(
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
            "/restaurant/employee/verify-forgot-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return whitelisted.stream().anyMatch(p -> pathMatcher.match(p, path));
    }
    
    @Override
    protected void doFilterInternal( HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("=== RESTAURANT SERVICE REQUEST ===");
        log.info("Path: {}", request.getServletPath());
        log.info("Headers: {}", Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(h -> h, request::getHeader)));

        String authUser = request.getHeader("X-Auth-User");
        String roles = request.getHeader("X-Roles");
        if (authUser == null) {
            log.info("No Auth User header found or it is invalid");
            sendErrorResponse(response,"Missing or invalid Auth User header");
            return;
        }
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authUser, null, getAuthorities(roles));

        log.info("Setting authentication in SecurityContext");
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        filterChain.doFilter(request, response);
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(String roles) {
        String[] roleList = roles.split(",");
        log.info("Extracted roles: {}", Arrays.stream(roleList).toList());
        return Arrays.stream(roleList)
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