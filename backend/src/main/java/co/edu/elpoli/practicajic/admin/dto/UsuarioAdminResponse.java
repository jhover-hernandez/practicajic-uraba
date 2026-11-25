package co.edu.elpoli.practicajic.admin.dto;

import co.edu.elpoli.practicajic.usuario.Rol;

import java.time.LocalDateTime;

public record UsuarioAdminResponse(Long id, String correo, String nombre, Rol rol, boolean activo,
                                   LocalDateTime fechaCreacion, String estadoPerfil) {
}
