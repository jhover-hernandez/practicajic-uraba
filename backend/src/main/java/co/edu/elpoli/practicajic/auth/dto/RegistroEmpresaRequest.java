package co.edu.elpoli.practicajic.auth.dto;

import jakarta.validation.constraints.*;

/** RF-01 (cuenta de empresa) + datos mínimos del perfil y aceptación de la política de datos (RNF-06). */
public record RegistroEmpresaRequest(
        @NotBlank(message = "Escribe el correo de la empresa.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
        String correo,

        @NotBlank(message = "Escribe una contraseña.")
        @Pattern(regexp = ReglasContrasena.PATRON, message = ReglasContrasena.MENSAJE)
        String contrasena,

        @NotBlank(message = "Escribe el NIT.")
        @Pattern(regexp = "^[0-9]{6,15}(-[0-9])?$", message = "El NIT debe tener el formato 900123456-7.")
        String nit,

        @NotBlank(message = "Escribe la razón social.")
        @Size(max = 150, message = "La razón social no puede superar 150 caracteres.")
        String razonSocial,

        @Size(max = 150, message = "El nombre comercial no puede superar 150 caracteres.")
        String nombreComercial,

        @NotBlank(message = "Selecciona el municipio.")
        @Size(max = 60, message = "El municipio no puede superar 60 caracteres.")
        String municipio,

        @Size(max = 150, message = "El contacto no puede superar 150 caracteres.")
        String contacto,

        @AssertTrue(message = "Debes aceptar la política de tratamiento de datos personales.")
        boolean aceptaPoliticaDatos) {
}
