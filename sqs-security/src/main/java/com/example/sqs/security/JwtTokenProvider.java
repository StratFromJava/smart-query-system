package com.example.sqs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析。
 *
 * <p>密钥长度必须 >= 32 字节（HS256 要求），否则启动时直接失败，避免弱密钥上线。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(@Value("${sqs.security.jwt.secret}") String secret,
                            @Value("${sqs.security.jwt.expiration-hours:12}") long expirationHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT 密钥长度不足 32 字节，请配置 sqs.security.jwt.secret");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationHours * 3600 * 1000;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    /**
     * 解析 token，失败时返回 null（不抛异常，交由过滤器统一处理）。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
