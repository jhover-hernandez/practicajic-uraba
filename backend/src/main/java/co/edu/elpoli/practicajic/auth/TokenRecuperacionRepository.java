package co.edu.elpoli.practicajic.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Al pedir un enlace nuevo, los anteriores dejan de servir. */
    @Modifying
    @Query("update TokenRecuperacion t set t.usado = true where t.usuario.id = :usuarioId and t.usado = false")
    int invalidarPendientes(Long usuarioId);
}
