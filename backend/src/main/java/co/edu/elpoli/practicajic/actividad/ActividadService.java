package co.edu.elpoli.practicajic.actividad;

import org.springframework.stereotype.Service;

/**
 * Punto único para dejar rastro de las acciones sensibles (RNF-05).
 * Los demás módulos lo usan al validar, aprobar o cambiar estados.
 */
@Service
public class ActividadService {

    private final RegistroActividadRepository repositorio;

    public ActividadService(RegistroActividadRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @param actorId usuario que ejecuta la acción; {@code null} si la ejecuta el sistema
     */
    public void registrar(Long actorId, String accion, String entidad, Long entidadId, String detalle) {
        repositorio.save(new RegistroActividad(actorId, accion, entidad, entidadId, detalle));
    }
}
