package co.edu.elpoli.practicajic.auth.dto;

/** Respuesta de login y renovación. El refresh token no viaja aquí: va en una cookie HttpOnly. */
public record SesionResponse(String accessToken, long expiraEnSegundos, UsuarioResponse usuario) {
}
