package br.com.petos.project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String ISSUER = "petos-api";
    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final int MINIMUM_SECRET_LENGTH = 32;

    private final SecretKey signingKey;
    private final Duration tokenExpiration;

    public JwtService(@Value("${petos.security.jwt.secret:}") String secret,
                      @Value("${petos.security.jwt.expiration-minutes:120}") long expirationMinutes) {
        this.signingKey = resolveSigningKey(secret);
        this.tokenExpiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenExpiration)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<String> extractSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rejeitado: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return tokenExpiration.toSeconds();
    }

    private SecretKey resolveSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("PETOS_JWT_SECRET nao configurado: gerando chave JWT efemera. "
                    + "Os tokens serao invalidados a cada reinicio. Configure a variavel de ambiente fora de desenvolvimento.");
            return Jwts.SIG.HS256.key().build();
        }
        if (secret.length() < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "petos.security.jwt.secret deve possuir no minimo " + MINIMUM_SECRET_LENGTH + " caracteres.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

