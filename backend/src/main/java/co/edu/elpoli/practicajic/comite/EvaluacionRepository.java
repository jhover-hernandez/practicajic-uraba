package co.edu.elpoli.practicajic.comite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByPostulacionIdOrderByFechaAsc(Long postulacionId);

    Optional<Evaluacion> findByPostulacionIdAndEvaluadorId(Long postulacionId, Long evaluadorId);

    List<Evaluacion> findByEvaluadorIdOrderByFechaDesc(Long evaluadorId);

    List<Evaluacion> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);
}
