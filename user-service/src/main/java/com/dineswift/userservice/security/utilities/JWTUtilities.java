package com.dineswift.userservice.security.utilities;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtilities {

    @Value("${jwt.secret-key}")
    public String secretkey;

    @Value("${jwt.expiration-time}")
    public long expirationTime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretkey.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .issuedAt(new Date())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        Claims body=extractClaims(token);
        return body.getSubject();
    }

    private boolean isTokenExpired(String token) {
        Claims claims=extractClaims(token);
        return claims.getExpiration().before(new Date(System.currentTimeMillis()));
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(UserDetails userDetails, String username, String token) {
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}
