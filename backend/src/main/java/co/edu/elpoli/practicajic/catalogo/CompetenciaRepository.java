package co.edu.elpoli.practicajic.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {

    List<Competencia> findAllByOrderByCategoriaAscNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
