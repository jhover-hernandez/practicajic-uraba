package co.edu.elpoli.practicajic.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "Escribe tu correo.")
        @Email(message = "El correo no tiene un formato válido.")
        String correo) {
}
