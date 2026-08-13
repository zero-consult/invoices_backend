package org.zero_consult.invoices_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.zero_consult.invoices_backend.configuration.CustomProperties;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import java.util.Date;

@Component
public class JwtUtil {

    private final CustomProperties customProperties;

    private SecretKey secretKey;

    private final JwtParser jwtParser;

    public final static String TOKEN_HEADER = "Authorization";
    public final static String TOKEN_PREFIX = "Bearer ";

    public JwtUtil(CustomProperties customProperties) {
        this.customProperties = customProperties;
        this.jwtParser = Jwts.parser().verifyWith(getSecretKey()).build();
    }

    private SecretKey getSecretKey() {
        if(secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(customProperties.getJwtSecretKey()));
        }
        return secretKey;
    }

    private Claims parseJwtClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }
}
