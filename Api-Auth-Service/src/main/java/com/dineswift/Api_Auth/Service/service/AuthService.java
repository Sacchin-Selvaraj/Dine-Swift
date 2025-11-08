package com.dineswift.Api_Auth.Service.service;

import com.dineswift.Api_Auth.Service.exception.AuthenticationException;
import com.dineswift.Api_Auth.Service.payload.*;
import com.dineswift.Api_Auth.Service.utilities.JwtUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;
    private final JwtUtilities jwtUtilities;

    public Mono<TokenResponse> authenticateUser(LoginRequest loginRequest, ServerHttpResponse response) {

        log.info("Based on the login type, forwarding the request ");
        Mono<TokenPair> tokenPair=null;
        if (loginRequest.getLoginType().equalsIgnoreCase("user")){
           tokenPair = authenticateWithUserService(loginRequest);
        } else if (loginRequest.getLoginType().equalsIgnoreCase("employee")){
            tokenPair = authenticateWithRestaurantService(loginRequest);
        }else {
            throw new AuthenticationException("Unsupported login type: " + loginRequest.getLoginType());
        }
        log.info("token pair generated: {}", tokenPair);
        return  tokenPair.doOnSuccess(tokenLog->log.info("Authentication process completed successfully"))
                .map(tokenPair1->{
                    ResponseCookie refreshCookie = getResponseCookie(tokenPair1.getRefreshToken());
                    response.getHeaders().add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                    TokenResponse tokenResponse = new TokenResponse();
                    tokenResponse.setAuthToken(tokenPair1.getAuthToken());
                    return tokenResponse;
                })
                .onErrorMap(error -> {
                    log.error("Authentication process failed");
                    String errorMessage = extractErrorMessage(error.getMessage());
                    throw new AuthenticationException(errorMessage);
                });
    }

    private Mono<TokenPair> authenticateWithRestaurantService(LoginRequest loginRequest) {
        log.info("Authenticating with Restaurant Service for email: {}", loginRequest.getEmail());

        return getResponseFromRestaurantService(loginRequest)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials provided")))
                .flatMap(employeeResponse -> {
                    log.info("Employee authenticated successfully with Restaurant Service");

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("authId", employeeResponse.getEmployeeId());
                    claims.put("roles", getEmployeeRoleName(employeeResponse));

                    log.info("Generating JWT tokens for employee: {}", employeeResponse.getEmployeeName());
                    String authToken = jwtUtilities.generateToken(claims, employeeResponse.getEmployeeName());
                    String refreshToken = jwtUtilities.generateRefreshToken(claims, employeeResponse.getEmployeeName(),loginRequest.isRememberMe());

                    TokenPair tokenPair = new TokenPair();
                    tokenPair.setAuthToken(authToken);
                    tokenPair.setRefreshToken(refreshToken);

                    return Mono.just(tokenPair);
                })
                .doOnError(error -> {
                    log.error("Employee authentication failed with Restaurant Service");
                });
    }

    private List<EmployeeRole> getEmployeeRoleName(EmployeeResponse employeeResponse) {
        log.info("Extracting roles for employee: {}", employeeResponse.getEmployeeName());
        return employeeResponse.getRoles().stream()
                .map(RoleDTOResponse::getRoleName)
                .toList();
    }

    private Mono<EmployeeResponse> getResponseFromRestaurantService(LoginRequest loginRequest) {
        log.info("Sending login request to Restaurant Service for email: {}", loginRequest.getEmail());
        Mono<EmployeeResponse> employeeResponse = webClient.post()
                .uri("http://restaurant-service/restaurant/employee/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(EmployeeResponse.class);
        log.info("Received response from Restaurant Service");
        return employeeResponse;
    }

    private static ResponseCookie getResponseCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh-token")
                .maxAge(Duration.ofDays(14))
                .build();
    }


    private Mono<TokenPair> authenticateWithUserService(LoginRequest loginRequest) {
        log.info("Authenticating with User Service for email: {}", loginRequest.getEmail());

        return getResponseFromUserService(loginRequest)
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials provided")))
                .flatMap(userResponse -> {
                    log.info("User authenticated successfully with User Service");

                    Map<String, Object> claims = new HashMap<>();
                    claims.put("authId", userResponse.getUserId());
                    claims.put("roles", getRoleName(userResponse));

                    log.info("Generating JWT tokens for user: {}", userResponse.getUsername());
                    String authToken = jwtUtilities.generateToken(claims, userResponse.getUsername());
                    String refreshToken = jwtUtilities.generateRefreshToken(claims, userResponse.getUsername(),loginRequest.isRememberMe());

                    TokenPair tokenPair = new TokenPair();
                    tokenPair.setAuthToken(authToken);
                    tokenPair.setRefreshToken(refreshToken);

                    return Mono.just(tokenPair);
                })
                .doOnError(error -> {
                    log.error("User authentication failed with User Service", error);
                });
    }

    private List<RoleName> getRoleName(UserResponse userResponse) {
        return userResponse.getRoles().stream()
                .map(RoleDto::getRoleName)
                .toList();
    }

    private Mono<UserResponse> getResponseFromUserService(LoginRequest loginRequest) {
        log.info("Sending login request to User Service for email: {}", loginRequest.getEmail());
        Mono<UserResponse> userResponse = webClient.post()
                .uri("http://user-service/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
        log.info("Received response from User Service");
        return userResponse;
    }

    public Mono<TokenResponse> refreshAuthToken(String refreshToken, ServerHttpResponse response) {

        log.info("Refreshing auth token using refresh token");
        if (!jwtUtilities.validateJwtToken(refreshToken))
            throw new AuthenticationException("Invalid or expired refresh token");

        Claims claimsFromRefreshToken = jwtUtilities.extractClaims(refreshToken);
        Map<String, Object> claims = new HashMap<>();
        claims.put("authId", claimsFromRefreshToken.get("authId"));
        claims.put("roles", claimsFromRefreshToken.get("roles"));
        String authUsername = claimsFromRefreshToken.getSubject();
        log.info("With Claims extracted, generating new auth token for user: {}", authUsername);
        String newAuthToken = jwtUtilities.generateToken(claims, authUsername);

        log.info("New auth token generated successfully");
        String newRefreshToken = jwtUtilities.generateRefreshToken(claims, authUsername, true);
        response.getHeaders().add(HttpHeaders.SET_COOKIE, getResponseCookie(newRefreshToken).toString());

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAuthToken(newAuthToken);

        return Mono.just(tokenResponse);
    }


    public void logoutUser(ServerHttpResponse response) {
        log.info("Logging out user by clearing refresh token cookie");
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh-token")
                .maxAge(0)
                .build();
        response.getHeaders().add(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        log.info("User logged out successfully, refresh token cookie cleared");
    }

    public String extractErrorMessage(String errorMessage) {
        String errorBody="";
        try {
            log.error("Error body from exception: {}", errorMessage);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(errorMessage);

            if (rootNode.has("errors") && rootNode.get("errors").isArray()) {
                for (JsonNode errorNode : rootNode.get("errors")) {
                    log.error("Error detail: {}", errorNode.asText());
                    errorBody = errorBody.concat(errorNode.asText()).concat(",");
                }
            }
        } catch (Exception e) {
            log.error("Failed to read error body from the Restaurant Service Response ", e);
            throw new AuthenticationException(
                    "Service call failed with status: " + errorMessage
            );
        }
        return errorBody;
    }
}
