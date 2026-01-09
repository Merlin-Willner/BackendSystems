package adapters.API;

import domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class JwtService {

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(
            @ConfigProperty(name = "app.jwt.secret") String secret,
            @ConfigProperty(name = "app.jwt.expiration-minutes", defaultValue = "30") long expirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim("userId", user.getUserId())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public UserPrincipal verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims body = claims.getBody();
            Number userIdValue = body.get("userId", Number.class);
            if (userIdValue == null || body.getSubject() == null) {
                return null;
            }
            return new UserPrincipal(userIdValue.longValue(), body.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    public long getExpirationMinutes() {
        return expiration.toMinutes();
    }
}
