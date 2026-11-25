package co.edu.elpoli.practicajic.practica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findByPracticaIdOrderByFechaAscIdAsc(Long practicaId);

    boolean existsByPracticaIdAndTipo(Long practicaId, Seguimiento.Tipo tipo);

    long countByPracticaId(Long practicaId);
}
