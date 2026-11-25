package co.edu.elpoli.practicajic.auth.dto;

import jakarta.validation.constraints.*;

/** RF-01 (cuenta de estudiante) + datos mínimos del perfil y aceptación de la política de datos (RNF-06). */
public record RegistroEstudianteRequest(
        @NotBlank(message = "Escribe tu correo.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,

        @NotBlank(message = "Escribe una contraseña.")
        @Pattern(regexp = ReglasContrasena.PATRON, message = ReglasContrasena.MENSAJE)
        String contrasena,

        @NotBlank(message = "Escribe tu número de documento.")
        @Pattern(regexp = "^[0-9]{5,20}$", message = "El documento debe tener solo números (5 a 20 dígitos).")
        String documento,

        @NotBlank(message = "Escribe tus nombres.")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres.")
        String nombres,

        @NotBlank(message = "Escribe tus apellidos.")
        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres.")
        String apellidos,

        @NotBlank(message = "Selecciona tu programa académico.")
        @Size(max = 120, message = "El programa no puede superar 120 caracteres.")
        String programa,

        @NotNull(message = "Selecciona tu semestre.")
        @Min(value = 1, message = "El semestre debe estar entre 1 y 12.")
        @Max(value = 12, message = "El semestre debe estar entre 1 y 12.")
        Short semestre,

        @Size(max = 60, message = "El municipio no puede superar 60 caracteres.")
        String municipio,

        @AssertTrue(message = "Debes aceptar la política de tratamiento de datos personales.")
        boolean aceptaPoliticaDatos) {
}
