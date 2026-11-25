package co.edu.elpoli.practicajic.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {

    List<Area> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
