package co.edu.elpoli.practicajic.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EstudianteCompetenciaRepository extends JpaRepository<EstudianteCompetencia, Long> {

    List<EstudianteCompetencia> findByEstudianteId(Long estudianteId);

    boolean existsByEstudianteId(Long estudianteId);

    @Modifying(flushAutomatically = true)
    @Query("delete from EstudianteCompetencia ec where ec.estudiante.id = :estudianteId")
    void eliminarDe(Long estudianteId);
}
