package com.dineswift.Api_Auth.Service.utilities;

import com.dineswift.Api_Auth.Service.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtilities {

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiration-time}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-time}")
    private long refreshTokenExpirationMs;

    public String generateToken(Map<String, Object> claims, String subject) {
        log.info("Generating JWT token for subject: {}", subject);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
                .subject(subject)
                .signWith(generateKey())
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> claims, String subject, boolean rememberMe) {
        log.info("Generating Refresh token for subject: {}", subject);
        refreshTokenExpirationMs = rememberMe?refreshTokenExpirationMs: 24 * 60 * 60 * 1000; // 1 day
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+refreshTokenExpirationMs))
                .subject(subject)
                .signWith(generateKey())
                .compact();
    }

    public SecretKey generateKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateJwtToken(String authToken) {
        log.info("Validating JWT token: {}", authToken);
        Claims claims = extractClaims(authToken);
        if (claims == null) {
            log.error("Failed to extract claims from JWT token");
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    public Claims extractClaims(String token) {
        try {
            log.info("Extracting claims from JWT token");
            return Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e){
            log.error("JWT token has expired: {}", e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error while parsing JWT token: {}", e.getMessage());
           return null;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }


}
