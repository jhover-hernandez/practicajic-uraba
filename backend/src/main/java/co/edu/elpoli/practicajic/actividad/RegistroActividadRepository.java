package co.edu.elpoli.practicajic.actividad;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Long> {

    List<RegistroActividad> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, Long entidadId);
}
