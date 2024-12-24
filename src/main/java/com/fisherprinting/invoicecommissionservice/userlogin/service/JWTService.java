package com.fisherprinting.invoicecommissionservice.userlogin.service;

import com.fisherprinting.invoicecommissionservice.userlogin.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JWTService {
    @Value("${app.secretKey}")
    private String SECRET;

    private static final long DURATION_VALIDITY = TimeUnit.HOURS.toMillis(7);

    public String generateToken(UserDetails user) {
        Map<String, String> claims = new HashMap<>();
        claims.put("iss", "Fisher Printing Inc.");
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(DURATION_VALIDITY)))
                .signWith(generateKey())
                .compact(); // Compact converts it to JSON format
    }

    private SecretKey generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET);

        // Convert to SecretKey Object
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String extractUsernameFromToken(String jwtToken) {
        Claims claims = getClaims(jwtToken);
        return claims.getSubject();
    }

    private Claims getClaims(String jwtToken) {
        Claims claims = Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
        return claims;
    }

    public boolean validateToken(String jwt) {
        Claims claims = getClaims(jwt);
        return claims.getExpiration().after(Date.from(Instant.now()));
    }
}
