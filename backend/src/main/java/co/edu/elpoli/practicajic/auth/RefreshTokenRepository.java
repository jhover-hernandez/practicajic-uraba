package co.edu.elpoli.practicajic.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Cierra todas las sesiones del usuario (desactivación, cambio de contraseña o robo de token). */
    @Modifying
    @Query("update RefreshToken t set t.revocado = true where t.usuario.id = :usuarioId and t.revocado = false")
    int revocarTodos(Long usuarioId);
}
