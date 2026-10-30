package co.edu.elpoli.practicajic.admin.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
        @NotNull(message = "Indica si la cuenta queda activa o inactiva.")
        Boolean activo) {
}
