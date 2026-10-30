package co.edu.elpoli.practicajic.seguridad;

import co.edu.elpoli.practicajic.usuario.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite y verifica los tokens de acceso JWT (RNF-02). Son de corta duración y se renuevan con el refresh token. */
@Service
public class JwtService {

    private static final String EMISOR = "practicajic-uraba";

    private final SecretKey clave;
    private final Duration duracionAcceso;

    public JwtService(@Value("${app.jwt.secreto}") String secreto,
                      @Value("${app.jwt.duracion-acceso}") Duration duracionAcceso) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.duracionAcceso = duracionAcceso;
    }

    public String generarAcceso(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(usuario.getId().toString())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionAcceso)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el id del usuario si el token es válido, está firmado por nosotros y no ha expirado. */
    public Optional<Long> usuarioIdSiValido(String token) {
        try {
            String sujeto = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.valueOf(sujeto));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Duration getDuracionAcceso() {
        return duracionAcceso;
    }
}
