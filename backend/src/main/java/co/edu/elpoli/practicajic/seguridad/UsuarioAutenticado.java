package co.edu.elpoli.practicajic.seguridad;

import co.edu.elpoli.practicajic.usuario.Rol;

/**
 * Usuario de la petición actual. Se obtiene en los controladores con
 * {@code @AuthenticationPrincipal UsuarioAutenticado actual}.
 */
public record UsuarioAutenticado(Long id, String correo, Rol rol) {
}
