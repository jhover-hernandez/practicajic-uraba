package co.edu.elpoli.practicajic.auth.dto;

/** Política de contraseñas compartida por registro, restablecimiento y creación de usuarios. */
public final class ReglasContrasena {

    /** Mínimo 8 caracteres con letras y números. Máximo 72 por el límite de BCrypt. */
    public static final String PATRON = "^(?=.*[A-Za-zÁÉÍÓÚáéíóúÑñ])(?=.*\\d).{8,72}$";

    public static final String MENSAJE = "La contraseña debe tener entre 8 y 72 caracteres e incluir letras y números.";

    private ReglasContrasena() {
    }
}
