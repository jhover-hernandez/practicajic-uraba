package co.edu.elpoli.practicajic.oferta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OfertaRepository extends JpaRepository<OfertaPractica, Long>, JpaSpecificationExecutor<OfertaPractica> {

    List<OfertaPractica> findByEmpresaIdOrderByFechaCreacionDesc(Long empresaId);

    Optional<OfertaPractica> findByIdAndEmpresaId(Long id, Long empresaId);

    List<OfertaPractica> findByEstadoOrderByFechaCreacionAsc(EstadoOferta estado);

    List<OfertaPractica> findByEstadoAndFechaCierreBefore(EstadoOferta estado, LocalDate fecha);

    long countByEstado(EstadoOferta estado);
}
