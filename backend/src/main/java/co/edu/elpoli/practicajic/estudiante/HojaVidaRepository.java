package co.edu.elpoli.practicajic.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HojaVidaRepository extends JpaRepository<HojaVida, Long> {

    Optional<HojaVida> findFirstByEstudianteIdAndActivaTrue(Long estudianteId);

    Optional<HojaVida> findFirstByEstudianteIdOrderByVersionDesc(Long estudianteId);

    boolean existsByEstudianteIdAndActivaTrue(Long estudianteId);

    @Modifying
    @Query("update HojaVida h set h.activa = false where h.estudiante.id = :estudianteId and h.activa = true")
    void desactivarTodas(Long estudianteId);
}
