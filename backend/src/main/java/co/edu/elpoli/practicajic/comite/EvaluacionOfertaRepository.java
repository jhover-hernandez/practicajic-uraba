package co.edu.elpoli.practicajic.comite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EvaluacionOfertaRepository extends JpaRepository<EvaluacionOferta, Long> {

    List<EvaluacionOferta> findByOfertaIdOrderByFechaDesc(Long ofertaId);

    List<EvaluacionOferta> findByEvaluadorIdOrderByFechaDesc(Long evaluadorId);

    List<EvaluacionOferta> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);
}
