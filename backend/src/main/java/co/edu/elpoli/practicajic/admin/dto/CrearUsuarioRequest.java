package co.edu.elpoli.practicajic.admin.dto;

import co.edu.elpoli.practicajic.auth.dto.ReglasContrasena;
import co.edu.elpoli.practicajic.usuario.Rol;
import jakarta.validation.constraints.*;

/** Cuentas internas del Politécnico. Estudiantes y empresas se registran por el formulario público. */
public record CrearUsuarioRequest(
        @NotBlank(message = "Escribe el correo.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,

        @NotBlank(message = "Escribe el nombre.")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
        String nombre,

        @NotNull(message = "Selecciona el rol.")
        Rol rol,

        @NotBlank(message = "Escribe una contraseña temporal.")
        @Pattern(regexp = ReglasContrasena.PATRON, message = ReglasContrasena.MENSAJE)
        String contrasena) {
}
