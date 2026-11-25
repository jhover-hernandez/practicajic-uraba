package co.edu.elpoli.practicajic.admin.dto;

import co.edu.elpoli.practicajic.usuario.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarUsuarioRequest(
        @NotBlank(message = "Escribe el correo.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,

        @NotBlank(message = "Escribe el nombre.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombre,

        @NotNull(message = "Selecciona el rol.")
        Rol rol) {
}
