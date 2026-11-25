package co.edu.elpoli.practicajic.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortafolioRepository extends JpaRepository<Portafolio, Long> {

    List<Portafolio> findByEstudianteIdOrderByIdAsc(Long estudianteId);

    Optional<Portafolio> findByIdAndEstudianteId(Long id, Long estudianteId);

    boolean existsByEstudianteId(Long estudianteId);
}
