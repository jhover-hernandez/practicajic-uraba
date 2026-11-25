package co.edu.elpoli.practicajic.practica;

import co.edu.elpoli.practicajic.usuario.Rol;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class PracticaDtos {

    private PracticaDtos() {
    }

    /** Estudiante que aceptó la práctica y espera que el Politécnico la formalice. */
    public record PendienteFormalizarDto(Long postulacionId, String estudiante, String programa, String oferta,
                                         String empresa, LocalDateTime fechaAceptacion, String tipoVinculacion,
                                         BigDecimal incentivo, Short duracionMeses) {
    }

    public record FormalizarRequest(
            @NotNull(message = "Selecciona la fecha de inicio.")
            LocalDate fechaInicio,

            LocalDate fechaFin,

            @Size(max = 60, message = "El tipo de vinculación no puede superar 60 caracteres.")
            String tipoVinculacion,

            @DecimalMin(value = "0", message = "El incentivo no puede ser negativo.")
            @Digits(integer = 10, fraction = 2, message = "El incentivo no tiene un formato válido.")
            BigDecimal incentivo,

            @NotBlank(message = "Escribe el nombre del tutor de la empresa.")
            @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
            String tutorEmpresa,

            @NotBlank(message = "Escribe el nombre del tutor del Politécnico.")
            @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
            String tutorInstitucion) {
    }

    public record SeguimientoRequest(
            @NotNull(message = "Selecciona el tipo de seguimiento.")
            Seguimiento.Tipo tipo,

            @NotNull(message = "Selecciona la fecha.")
            @PastOrPresent(message = "La fecha no puede estar en el futuro.")
            LocalDate fecha,

            @NotBlank(message = "Describe el seguimiento.")
            @Size(max = 4000, message = "La descripción no puede superar 4000 caracteres.")
            String descripcion,

            @DecimalMin(value = "0", message = "La calificación va de 0 a 5.")
            @DecimalMax(value = "5", message = "La calificación va de 0 a 5.")
            @Digits(integer = 1, fraction = 1, message = "Usa un decimal, por ejemplo 4.5")
            BigDecimal calificacion) {
    }

    public record SeguimientoDto(Long id, LocalDate fecha, Seguimiento.Tipo tipo, String descripcion,
                                 BigDecimal calificacion, String responsable, Rol rolResponsable) {
        public static SeguimientoDto de(Seguimiento s) {
            return new SeguimientoDto(s.getId(), s.getFecha(), s.getTipo(), s.getDescripcion(), s.getCalificacion(),
                    s.getResponsable().getNombre(), s.getResponsable().getRol());
        }
    }

    public record PracticaDto(Long id, Long postulacionId, Long estudianteId, String estudiante, String correoEstudiante,
                              String programa, String oferta, String empresa, String area, LocalDate fechaInicio,
                              LocalDate fechaFin, String tipoVinculacion, BigDecimal incentivo, String tutorEmpresa,
                              String tutorInstitucion, EstadoPractica estado, List<SeguimientoDto> seguimientos,
                              BigDecimal calificacionFinal) {
    }
}
