package co.edu.elpoli.practicajic.postulacion;

import java.util.EnumSet;
import java.util.Set;

/**
 * Flujo de la postulación (SRS, sección 5). Avanza en un solo sentido y puede salir a NO_SELECCIONADO
 * desde la revisión, la preselección o la entrevista.
 */
public enum EstadoPostulacion {
    POSTULADO,
    EN_REVISION,
    PRESELECCIONADO,
    ENTREVISTA,
    SELECCIONADO,
    NO_SELECCIONADO,
    PRACTICA_INICIADA,
    PRACTICA_FINALIZADA;

    /** Estados que ocupan una vacante de la oferta (RN-07). */
    public static final Set<EstadoPostulacion> OCUPAN_VACANTE = EnumSet.of(SELECCIONADO, PRACTICA_INICIADA, PRACTICA_FINALIZADA);

    /** Estados en los que el proceso sigue abierto. */
    public static final Set<EstadoPostulacion> EN_PROCESO = EnumSet.of(POSTULADO, EN_REVISION, PRESELECCIONADO, ENTREVISTA);

    /** Siguiente estado del flujo que la empresa o el comité pueden asignar, o {@code null} si no hay. */
    public EstadoPostulacion siguiente() {
        return switch (this) {
            case POSTULADO -> EN_REVISION;
            case EN_REVISION -> PRESELECCIONADO;
            case PRESELECCIONADO -> ENTREVISTA;
            case ENTREVISTA -> SELECCIONADO;
            default -> null;
        };
    }

    public boolean puedePasarA(EstadoPostulacion nuevo) {
        if (nuevo == NO_SELECCIONADO) {
            return this == EN_REVISION || this == PRESELECCIONADO || this == ENTREVISTA;
        }
        return nuevo == siguiente();
    }
}
