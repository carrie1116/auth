package com.example.auth.service;

import com.example.auth.persistence.model.AuthenticatedUser;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {

        AuthenticatedUser userPrincipal = (AuthenticatedUser) authentication.getPrincipal();

        DateTime now = new DateTime(DateTimeZone.UTC);
        DateTime expiryDate = now.plusMillis(jwtExpirationInMs);

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("type", "JWT");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put(PayloadKey.sub.name(), userPrincipal.getUsername());
        payloadMap.put(PayloadKey.name.name(), userPrincipal.getUser().getName());
        payloadMap.put(PayloadKey.iat.name(), getDate(now));
        payloadMap.put(PayloadKey.exp.name(), getDate(expiryDate));
        payloadMap.put(PayloadKey.roles.name(), userPrincipal.getAuthorities().stream().map(
            GrantedAuthority::getAuthority).collect(Collectors.toList()));

        Gson gson = new Gson();
        String payload = gson.toJson(payloadMap);

        return Jwts.builder()
            .setHeader(headerMap)
            .setPayload(payload)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    private long getDate(DateTime d) {
        return d.getMillis();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
    }

    public String getUsernameFromJWT(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getRolesFromJWT(String token) {
        return (List) getClaims(token).get(PayloadKey.roles.name());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            if ((new Date()).before(claims.getExpiration())) {
                return true;
            }
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    enum PayloadKey {sub, name, admin, iat, exp, roles}
}
