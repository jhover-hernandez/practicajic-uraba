package co.edu.elpoli.practicajic.postulacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByOfertaIdAndEstudianteId(Long ofertaId, Long estudianteId);

    List<Postulacion> findByEstudianteIdOrderByFechaDesc(Long estudianteId);

    List<Postulacion> findByOfertaIdOrderByCompetenciasCoincidentesDescFechaAsc(Long ofertaId);

    List<Postulacion> findByOfertaEmpresaIdOrderByFechaDesc(Long empresaId);

    List<Postulacion> findByEstadoInOrderByFechaAsc(Collection<EstadoPostulacion> estados);

    List<Postulacion> findByEstudianteIdAndEstadoIn(Long estudianteId, Collection<EstadoPostulacion> estados);

    long countByOfertaId(Long ofertaId);

    long countByOfertaIdAndEstadoIn(Long ofertaId, Collection<EstadoPostulacion> estados);

    boolean existsByEstudianteIdAndEstadoIn(Long estudianteId, Collection<EstadoPostulacion> estados);
}
