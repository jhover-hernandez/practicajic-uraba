package co.edu.elpoli.practicajic.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    List<Estudiante> findByEstadoOrderByIdAsc(EstadoEstudiante estado);

    List<Estudiante> findAllByOrderByApellidosAscNombresAsc();

    long countByEstado(EstadoEstudiante estado);

    boolean existsByDocumento(String documento);
}
