package co.edu.elpoli.practicajic.comun;

import org.springframework.http.HttpStatus;

/** Error esperado de una regla de negocio. El mensaje se muestra tal cual al usuario (RNF-11). */
public class NegocioException extends RuntimeException {

    private final HttpStatus estado;

    public NegocioException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
