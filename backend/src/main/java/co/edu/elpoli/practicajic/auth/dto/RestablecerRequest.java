package co.edu.elpoli.practicajic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RestablecerRequest(
        @NotBlank(message = "El enlace de recuperación no es válido.")
        String token,

        @NotBlank(message = "Escribe la nueva contraseña.")
        @Pattern(regexp = ReglasContrasena.PATRON, message = ReglasContrasena.MENSAJE)
        String nuevaContrasena) {
}
