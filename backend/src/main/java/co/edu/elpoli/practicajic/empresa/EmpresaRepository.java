package co.edu.elpoli.practicajic.empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByUsuarioId(Long usuarioId);

    List<Empresa> findByEstadoValidacionOrderByIdAsc(EstadoValidacionEmpresa estado);

    List<Empresa> findAllByOrderByRazonSocialAsc();

    long countByEstadoValidacion(EstadoValidacionEmpresa estado);

    boolean existsByNit(String nit);
}
