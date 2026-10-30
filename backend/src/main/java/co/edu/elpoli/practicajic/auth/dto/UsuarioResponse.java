package co.edu.elpoli.practicajic.auth.dto;

import co.edu.elpoli.practicajic.usuario.Rol;

/**
 * Datos del usuario en sesión. {@code estadoPerfil} es el estado de validación del estudiante o la empresa
 * (PENDIENTE, VALIDADO, ...); es {@code null} para administradores y comité.
 */
public record UsuarioResponse(Long id, String correo, String nombre, Rol rol, String estadoPerfil) {
}
