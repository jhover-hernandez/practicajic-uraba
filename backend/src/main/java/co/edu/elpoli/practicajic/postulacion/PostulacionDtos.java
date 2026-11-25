package co.edu.elpoli.practicajic.postulacion;

import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.CompetenciaEstudianteDto;
import co.edu.elpoli.practicajic.estudiante.EstudianteDtos.PortafolioDto;
import co.edu.elpoli.practicajic.oferta.Modalidad;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PostulacionDtos {

    private PostulacionDtos() {
    }

    /** RF-23: postulación vista por el estudiante. */
    public record MiPostulacionDto(Long id, Long ofertaId, String oferta, String empresa, String municipio,
                                   Modalidad modalidad, LocalDateTime fecha, EstadoPostulacion estado,
                                   int coincidentes, int totalCompetencias, LocalDateTime fechaEntrevista,
                                   String lugarEntrevista, String observaciones, LocalDateTime fechaAceptacion,
                                   boolean puedeAceptar) {
    }

    /** RF-10 y RF-25: postulante visto por la empresa o por el comité. */
    public record PostulanteDto(Long id, Long ofertaId, String oferta, Long estudianteId, String nombre, String correo,
                                String programa, Short semestre, String municipio, String disponibilidad,
                                LocalDateTime fecha, EstadoPostulacion estado, EstadoPostulacion siguienteEstado,
                                int coincidentes, int totalCompetencias, List<Long> competenciasCoincidentes,
                                List<CompetenciaEstudianteDto> competencias, List<PortafolioDto> portafolio,
                                boolean tieneHojaVida, LocalDateTime fechaEntrevista, String lugarEntrevista,
                                String observaciones, LocalDateTime fechaAceptacion, Long evaluadorId,
                                String evaluador, ResumenComiteDto comite) {
    }

    /** Lo que el comité dijo del candidato (RF-31, RF-32). */
    public record ResumenComiteDto(int evaluaciones, BigDecimal puntajePromedio, boolean recomendado,
                                   List<String> conceptos) {
    }

    public record CambioEstadoRequest(
            @NotNull(message = "Selecciona el nuevo estado.")
            EstadoPostulacion estado,

            @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres.")
            String observaciones,

            /** Obligatoria al pasar a ENTREVISTA (RF-26). */
            @Future(message = "La entrevista debe programarse en una fecha futura.")
            LocalDateTime fechaEntrevista,

            @Size(max = 200, message = "El lugar no puede superar 200 caracteres.")
            String lugarEntrevista) {
    }

    public record EntrevistaRequest(
            @NotNull(message = "Selecciona la fecha y hora de la entrevista.")
            @Future(message = "La entrevista debe programarse en una fecha futura.")
            LocalDateTime fechaEntrevista,

            @Size(max = 200, message = "El lugar no puede superar 200 caracteres.")
            String lugarEntrevista) {
    }
}
