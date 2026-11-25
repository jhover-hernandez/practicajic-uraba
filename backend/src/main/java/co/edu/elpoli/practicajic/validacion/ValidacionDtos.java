package co.edu.elpoli.practicajic.validacion;

import co.edu.elpoli.practicajic.estudiante.EstadoEstudiante;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ValidacionDtos {

    private ValidacionDtos() {
    }

    public enum Decision {
        VALIDAR,
        RECHAZAR,
        INACTIVAR
    }

    public record DecisionRequest(
            @NotNull(message = "Selecciona la decisión.")
            Decision decision,

            @Size(max = 1000, message = "El motivo no puede superar 1000 caracteres.")
            String motivo) {
    }

    public record EstudianteResumenDto(Long id, String nombre, String documento, String correo, String programa,
                                       Short semestre, String municipio, EstadoEstudiante estado,
                                       String motivoRechazo, LocalDateTime fechaRegistro, boolean tieneHojaVida) {
    }
}
