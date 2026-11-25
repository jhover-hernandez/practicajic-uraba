package co.edu.elpoli.practicajic.practica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticaRepository extends JpaRepository<Practica, Long> {

    boolean existsByPostulacionId(Long postulacionId);

    List<Practica> findAllByOrderByFechaInicioDesc();

    List<Practica> findByEstadoOrderByFechaInicioDesc(EstadoPractica estado);

    List<Practica> findByPostulacionOfertaEmpresaIdOrderByFechaInicioDesc(Long empresaId);

    List<Practica> findByPostulacionEstudianteIdOrderByFechaInicioDesc(Long estudianteId);
}
