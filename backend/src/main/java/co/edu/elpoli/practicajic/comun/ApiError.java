package co.edu.elpoli.practicajic.comun;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de error de la API. {@code campos} trae el error de cada campo inválido. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int estado, String mensaje, Map<String, String> campos, LocalDateTime fecha) {

    public static ApiError de(int estado, String mensaje) {
        return new ApiError(estado, mensaje, null, LocalDateTime.now());
    }
}
