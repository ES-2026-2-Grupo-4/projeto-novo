package com.esproject4.arenaufcg.Service.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.esproject4.arenaufcg.Exception.RegraNegocioException;
import com.esproject4.arenaufcg.Model.User;
import com.esproject4.arenaufcg.Model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Implementação concreta da porta TokenProvider usando JWT (HS256) via biblioteca jjwt.
@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey chave;
    private final long validadeMs;

    public JwtTokenProvider(
            // Em produção, defina app.jwt.secret por variável de ambiente. HS256 exige >=
            // 32 bytes.
            @Value("${app.jwt.secret:troque-esta-chave-em-producao-arena-ufcg-jwt-secret-key-0001}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long validadeMs) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validadeMs = validadeMs;
    }

    @Override
    public String gerarToken(User usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail().address())
                .claim("role", usuario.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusMillis(validadeMs)))
                .signWith(chave)
                .compact();
    }

    @Override
    public TokenClaims validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenClaims(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            // Assinatura adulterada, token expirado, malformado ou claims inválidos.
            throw new RegraNegocioException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO",
                    "Token ausente, malformado, adulterado ou expirado.");
        }
    }
}
