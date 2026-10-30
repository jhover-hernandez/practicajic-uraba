package co.edu.elpoli.practicajic.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    boolean existsByDocumento(String documento);
}
